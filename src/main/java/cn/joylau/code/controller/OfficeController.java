package cn.joylau.code.controller;


import cn.joylau.code.service.FTPService;
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
import java.io.InputStream;

@RequestMapping("office")
@RestController
@Data
public class OfficeController {

    @Value("${file.remote-addr}")
    private String remoteAddr;

    private final DocumentConverter converter;
    private final FTPService ftpService;
    @Autowired
    public OfficeController(DocumentConverter converter, FTPService ftpService) {
        this.converter = converter;
        this.ftpService = ftpService;
    }

    @GetMapping("/preview/{fileName}")
    public Object preview(@PathVariable String fileName){
        try {
            Resource resource = new UrlResource(remoteAddr + fileName);
            if (FilenameUtils.getExtension(resource.getFilename()).equalsIgnoreCase("pdf")) {
                return "Is the PDF file";
            }
            return convert(resource.getInputStream(),FilenameUtils.getExtension(resource.getFilename()));
        } catch (IOException e) {
            e.printStackTrace();
            return "File does not exist;";
        }
    }


    @GetMapping("/previewFTPFile")
    public Object previewFTPFile(String remoteRelativePath){
        try {
            InputStream inputStream = ftpService.downStreamFile(remoteRelativePath);
            if (inputStream == null) {
                return "The file cannot be read;";
            }
            String remoteFileName;
            if (remoteRelativePath.contains("/")) {
                int index = remoteRelativePath.lastIndexOf("/");
                remoteFileName = remoteRelativePath.substring(index + 1);
            } else {
                remoteFileName = remoteRelativePath;
            }
            if (FilenameUtils.getExtension(remoteFileName).equalsIgnoreCase("pdf")) {
                return "Is the PDF file";
            }
            return convert(inputStream,FilenameUtils.getExtension(remoteFileName));
        } catch (Exception e) {
            e.printStackTrace();
            return "The file cannot be read;";
        }
    }

    /**
     * 转化方法
     * @param inputStream 输入文件流
     * @param inputFileExtension 输入文件扩展名
     * @return object
     */
    private Object convert(InputStream inputStream, String inputFileExtension) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final DocumentFormat targetFormat = DefaultDocumentFormatRegistry.getFormatByExtension("pdf");
            converter
                    .convert(inputStream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension(inputFileExtension))
                    .to(baos)
                    .as(targetFormat)
                    .execute();

            inputStream.close();
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        } catch (OfficeException | IOException e) {
            e.printStackTrace();
            return "convert error: " + e.getMessage();
        }
    }
}
