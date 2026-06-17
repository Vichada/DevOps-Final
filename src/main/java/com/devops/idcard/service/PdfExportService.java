package com.devops.idcard.service;

import com.devops.idcard.model.Profile;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService {

    public byte[] generateIdCardPdf(Profile profile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Structure a simple document layout block representing the printable card boundaries
            document.add(new Paragraph("OFFICIAL ID CARD").setBold().setFontSize(16));
            document.add(new Paragraph("----------------------------------------"));
            
            Table table = new Table(2);
            table.addCell("Registration No:");
            table.addCell(profile.getRegistrationNumber());
            table.addCell("Full Name:");
            table.addCell(profile.getFullName());
            table.addCell("Department:");
            table.addCell(profile.getDepartment() != null ? profile.getDepartment() : "General");
            table.addCell("Type:");
            table.addCell(profile.getType().toString());
            
            document.add(table);
            document.add(new Paragraph("----------------------------------------"));
            document.add(new Paragraph("Verification UUID: " + profile.getUuid()).setFontSize(8));
            
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return out.toByteArray();
    }
}
