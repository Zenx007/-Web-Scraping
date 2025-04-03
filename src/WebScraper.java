import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class WebScraper {
    private static final String URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String DIR = "anexos";
    private static final String ZIP = "anexos.zip";

    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get(DIR));

            Document doc = Jsoup.connect(URL).timeout(30000).get();
            Set<String> urls = new LinkedHashSet<>();

            for (Element link : doc.select("a[href$=.pdf]")) {
                String text = link.text().toLowerCase();
                String href = link.attr("abs:href");
                if ((text.contains("anexo i") || text.contains("anexo ii"))) {
                    urls.add(href);
                    System.out.println("Anexo encontrado: " + href);
                }
            }

            Set<String> files = new LinkedHashSet<>();
            for (String url : urls) {
                String name = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
                String path = DIR + "/" + name;

                System.out.println("Baixando: " + name);
                try (InputStream in = new URL(url).openStream()) {
                    Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                    files.add(path);
                }
            }

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(ZIP))) {
                for (String file : files) {
                    zos.putNextEntry(new ZipEntry(new File(file).getName()));
                    Files.copy(Paths.get(file), zos);
                    zos.closeEntry();
                }
            }

            System.out.println("Concluído! Arquivo ZIP criado: " + ZIP);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}