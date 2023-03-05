package cloud.projects.classifier.service.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import cloud.projects.library.dto.ClassificationResult;
import cloud.projects.library.exception.ClassificationException;
import cloud.projects.library.service.ClassificationService;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.IoUtils;

@Log4j2
@Service
public class ClassificationServicePythonImpl implements ClassificationService {

	private static final Gson GSON = new Gson();

	@Value("${app.script}")
	private String script;

	@Value("${app.script-path}")
	private String scriptPath;

	@Override
	public ClassificationResult classify(String key, File image) {

		final ProcessBuilder builder = new ProcessBuilder(script, scriptPath, image.getAbsolutePath());
		builder.redirectErrorStream(true);

		// running the script
		Process process = null;
		String result = null;
		int code = 0;
		try {
			process = builder.start();
			result = IoUtils.toUtf8String(process.getInputStream());
			code = process.waitFor();
		} catch (IOException | InterruptedException ex) {
			log.error(ex.getMessage(), ex);
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new ClassificationException(ex);
		}

		// Checking if script just fine
		if (code != 0) {
			val errorMsg = String.format("Script ended with error code %s! result: %s", code, result);
			log.error(errorMsg);
			throw new ClassificationException(errorMsg);
		}
		return GSON.fromJson(result, ClassificationResult.class);
	}

}
