package com.clicker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Handles persistence with AES encryption and HMAC integrity protection.
 */
public class SaveManager {
    private static final String PASSWORD = "local-strong-passphrase-change-me";
    private static final String HMAC_ALGO = "HmacSHA256";

    public void save(File file, SaveData data) throws Exception {
        String xml = toXml(data);
        byte[] serialized = xml.getBytes(StandardCharsets.UTF_8);
        byte[] iv = randomBytes(16);
        SecretKey key = deriveKey(PASSWORD.toCharArray());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(serialized);

        byte[] hmac = createHmac(key, iv, cipherText);

        String payload = "HMAC:" + Base64.getEncoder().encodeToString(hmac) + "\n" +
                "DATA:" + Base64.getEncoder().encodeToString(concat(iv, cipherText));
        java.nio.file.Files.writeString(file.toPath(), payload, StandardCharsets.UTF_8);
    }

    public SaveData load(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        if (lines.size() < 2) {
            throw new IllegalStateException("Save file malformed");
        }
        String hmacLine = lines.get(0).replace("HMAC:", "");
        String dataLine = lines.get(1).replace("DATA:", "");
        byte[] expectedHmac = Base64.getDecoder().decode(hmacLine);
        byte[] cipherPayload = Base64.getDecoder().decode(dataLine);
        byte[] iv = new byte[16];
        System.arraycopy(cipherPayload, 0, iv, 0, iv.length);
        byte[] cipherText = new byte[cipherPayload.length - iv.length];
        System.arraycopy(cipherPayload, iv.length, cipherText, 0, cipherText.length);

        SecretKey key = deriveKey(PASSWORD.toCharArray());
        byte[] actual = createHmac(key, iv, cipherText);
        if (!MessageDigest.isEqual(expectedHmac, actual)) {
            throw new SecurityException("Save integrity check failed");
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(cipherText);
        String xml = new String(decrypted, StandardCharsets.UTF_8);
        return fromXml(xml);
    }

    private SecretKey deriveKey(char[] password) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, "inventory-salt".getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] randomBytes(int length) {
        byte[] buffer = new byte[length];
        new SecureRandom().nextBytes(buffer);
        return buffer;
    }

    private byte[] concat(byte[] iv, byte[] cipherText) {
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        return combined;
    }

    private byte[] createHmac(SecretKey key, byte[] iv, byte[] cipherText) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(key.getEncoded(), HMAC_ALGO));
        mac.update(iv);
        mac.update(cipherText);
        return mac.doFinal();
    }

    private void appendElement(Document doc, Element parent, String name, String value) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value));
        parent.appendChild(child);
    }

    private String toXml(SaveData data) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("save");
        doc.appendChild(root);

        appendElement(doc, root, "resources", String.valueOf(data.resources));
        appendElement(doc, root, "clickValue", String.valueOf(data.clickValue));
        appendElement(doc, root, "passiveIncome", String.valueOf(data.passiveIncome));
        appendElement(doc, root, "autoClickPerSecond", String.valueOf(data.autoClickPerSecond));
        appendElement(doc, root, "lastSave", data.lastSave.toString());

        Element skins = doc.createElement("skins");
        root.appendChild(skins);
        for (Skin skin : data.skins) {
            Element node = doc.createElement("skin");
            node.setAttribute("id", skin.getId());
            node.setAttribute("name", skin.getName());
            node.setAttribute("rarity", skin.getRarity().name());
            node.setAttribute("icon", skin.getIconPath());
            skins.appendChild(node);
        }

        Element upgrades = doc.createElement("upgrades");
        root.appendChild(upgrades);
        for (SaveData.UpgradeSnapshot snapshot : data.upgrades) {
            Element node = doc.createElement("upgrade");
            node.setAttribute("id", snapshot.id);
            node.setAttribute("quantity", String.valueOf(snapshot.quantity));
            upgrades.appendChild(node);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString(StandardCharsets.UTF_8);
    }

    private SaveData fromXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        SaveData data = new SaveData();
        data.resources = Double.parseDouble(doc.getElementsByTagName("resources").item(0).getTextContent());
        data.clickValue = Double.parseDouble(doc.getElementsByTagName("clickValue").item(0).getTextContent());
        data.passiveIncome = Double.parseDouble(doc.getElementsByTagName("passiveIncome").item(0).getTextContent());
        data.autoClickPerSecond = Double.parseDouble(doc.getElementsByTagName("autoClickPerSecond").item(0).getTextContent());
        data.lastSave = java.time.Instant.parse(doc.getElementsByTagName("lastSave").item(0).getTextContent());

        NodeList skinNodes = doc.getElementsByTagName("skin");
        for (int i = 0; i < skinNodes.getLength(); i++) {
            Element element = (Element) skinNodes.item(i);
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            SkinRarity rarity = SkinRarity.valueOf(element.getAttribute("rarity"));
            String icon = element.getAttribute("icon");
            data.skins.add(new Skin(id, name, rarity, icon));
        }

        NodeList upgradeNodes = doc.getElementsByTagName("upgrade");
        for (int i = 0; i < upgradeNodes.getLength(); i++) {
            Element element = (Element) upgradeNodes.item(i);
            SaveData.UpgradeSnapshot snapshot = new SaveData.UpgradeSnapshot();
            snapshot.id = element.getAttribute("id");
            snapshot.quantity = Integer.parseInt(element.getAttribute("quantity"));
            data.upgrades.add(snapshot);
        }
        return data;
    }

    public void applyLoadedData(SaveData data, GameState state, Inventory inventory, List<Upgrade> upgrades) {
        if (data == null) {
            return;
        }
        state.setResources(data.resources);
        state.setClickValue(data.clickValue);
        state.setPassiveIncome(data.passiveIncome);
        state.setAutoClickPerSecond(data.autoClickPerSecond);
        state.setLastSave(data.lastSave);

        inventory.setSkins(data.skins);
        for (SaveData.UpgradeSnapshot snapshot : data.upgrades) {
            upgrades.stream()
                    .filter(u -> u.getId().equals(snapshot.id))
                    .findFirst()
                    .ifPresent(u -> {
                        for (int i = 0; i < snapshot.quantity; i++) {
                            u.increaseQuantity();
                        }
                        state.syncUpgradeQuantity(u.getId(), snapshot.quantity);
                    });
        }
    }
}
