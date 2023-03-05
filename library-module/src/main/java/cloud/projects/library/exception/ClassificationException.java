package cloud.projects.library.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClassificationException extends RuntimeException {

	private static final long serialVersionUID = 3288014087717308888L;

	public ClassificationException(String message) {
		super(message);
	}

	public ClassificationException(Throwable throwable) {
		super(throwable);
	}

}
