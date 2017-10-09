import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DSV6Analyzer analyzer = null;
            try {
                analyzer = new DSV6Analyzer();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            analyzer.analyzeFile(new File("C:/test.dsv6"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }
    }

}
