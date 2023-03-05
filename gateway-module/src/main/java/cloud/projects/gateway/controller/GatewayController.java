package cloud.projects.gateway.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cloud.projects.library.dto.ClassificationResult;
import cloud.projects.library.service.ClassificationService;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/classifier")
public class GatewayController {

	@Autowired
	private ClassificationService service;

	@PostMapping
	public ClassificationResult classify(@RequestParam MultipartFile image) throws IOException {
		log.info("Receveied request for image: {}", image.getOriginalFilename());
		return service.classify(extractKey(image), createFile(image));
	}

	private String extractKey(MultipartFile image) {
		val filename = image.getOriginalFilename();
		return Objects.isNull(filename) ? "test_?" : filename.split("[.]")[0];
	}

	private File createFile(MultipartFile image) throws IOException {
		val file = File.createTempFile("image", null);
		try (val stream = new FileOutputStream(file)) {
			stream.write(image.getBytes());
		}
		return file;
	}

}
