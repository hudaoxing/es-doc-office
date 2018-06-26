package cn.joylau.code.controller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class ConverterController {
    private static final String ATTRNAME_ERROR_MESSAGE = "errorMessage";
    private static final String ON_ERROR_REDIRECT = "redirect:/";

    private final DocumentConverter converter;

    @Autowired
    public ConverterController(DocumentConverter converter) {
        this.converter = converter;
    }

    @GetMapping("/")
    public String index() {
        return "converter";
    }

    @PostMapping("/converter")
    public Object convert(
            @RequestParam("inputFile") final MultipartFile inputFile,
            @RequestParam(name = "outputFormat", required = false) final String outputFormat,
            final RedirectAttributes redirectAttributes) {

        if (inputFile.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    ATTRNAME_ERROR_MESSAGE, "Please select a file to upload.");
            return ON_ERROR_REDIRECT;
        }

        if (StringUtils.isBlank(outputFormat)) {
            redirectAttributes.addFlashAttribute(
                    ATTRNAME_ERROR_MESSAGE, "Please select an output format.");
            return ON_ERROR_REDIRECT;
        }

        // Here, we could have a dedicated service that would convert document
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final DocumentFormat targetFormat =
                    DefaultDocumentFormatRegistry.getFormatByExtension(outputFormat);
            converter
                    .convert(inputFile.getInputStream())
                    .as(
                            DefaultDocumentFormatRegistry.getFormatByExtension(
                                    FilenameUtils.getExtension(inputFile.getOriginalFilename())))
                    .to(baos)
                    .as(targetFormat)
                    .execute();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
            headers.add(
                    "Content-Disposition",
                    "attachment; filename="
                            + FilenameUtils.getBaseName(inputFile.getOriginalFilename())
                            + "."
                            + targetFormat.getExtension());
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (OfficeException | IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    ATTRNAME_ERROR_MESSAGE,
                    "Unable to convert the file "
                            + inputFile.getOriginalFilename()
                            + ". Cause: "
                            + e.getMessage());
        }

        return ON_ERROR_REDIRECT;
    }
}
