package com.devops.idcard.service;

import com.devops.idcard.model.BarcodeType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class CardMediaService {

    // Generates a QR Code containing a secure lookup payload string
    public String generateQRCodeBase64(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    // Generates the Barcode dynamically using the selected Enum format (CODE_128 or EAN_13)
    public String generateBarcodeBase64(String text, BarcodeType type, int width, int height) throws Exception {
        BitMatrix bitMatrix;
        if (type == BarcodeType.EAN_13) {
            // EAN-13 expects exactly 12 or 13 digits, sanitize text or fallback to Code128 if text has characters
            String numericText = text.replaceAll("[^0-8]", "");
            if (numericText.length() < 12) {
                numericText = String.format("%012d", Long.parseLong(numericText.isEmpty() ? "0" : numericText));
            }
            numericText = numericText.substring(0, 12);
            bitMatrix = new EAN13Writer().encode(numericText, BarcodeFormat.EAN_13, width, height);
        } else {
            bitMatrix = new Code128Writer().encode(text, BarcodeFormat.CODE_128, width, height);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
