package cn.joylau.code.controller;


import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RequestMapping("office")
@RestController
@Data
public class OfficeController {

    @Value("${file.remote-addr}")
    private String remoteAddr;

    private final DocumentConverter converter;

    @Autowired
    public OfficeController(DocumentConverter converter) {
        this.converter = converter;
    }

    @GetMapping("/preview/{fileName}")
    public Object preview(@PathVariable String fileName){
        try {
            Resource resource = new UrlResource(remoteAddr + fileName);
            if (FilenameUtils.getExtension(resource.getFilename()).equalsIgnoreCase("pdf")) {
                return "Is the PDF file";
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                final DocumentFormat targetFormat =
                        DefaultDocumentFormatRegistry.getFormatByExtension("pdf");
                converter
                        .convert(resource.getInputStream())
                        .as(
                                DefaultDocumentFormatRegistry.getFormatByExtension(
                                        FilenameUtils.getExtension(resource.getFilename())))
                        .to(baos)
                        .as(targetFormat)
                        .execute();

                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
                return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

            } catch (OfficeException | IOException e) {
                e.printStackTrace();
                return "convert error: " + e.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "File does not exist;";
        }
    }
}
