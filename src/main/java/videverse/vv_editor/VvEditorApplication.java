package videverse.vv_editor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VvEditorApplication {

	public static void main(String[] args) {
		//starts here
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to load SQLite JDBC driver", e);
		}
		SpringApplication.run(VvEditorApplication.class, args);
	}

}
