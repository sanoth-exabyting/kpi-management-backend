package org.example.kpiservice.service;

import org.thymeleaf.context.Context;

public interface PdfGenerationService {
    byte[] generatePdfFromTemplate(String templateName, Context context);
}
