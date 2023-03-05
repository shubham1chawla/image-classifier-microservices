package cloud.projects.library.service;

import java.io.File;

import cloud.projects.library.dto.ClassificationResult;

public interface ClassificationService {

	ClassificationResult classify(String key, File image);

}
