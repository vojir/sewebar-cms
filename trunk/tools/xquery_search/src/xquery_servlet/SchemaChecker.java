package xquery_servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 * Trida slouzici k validaci vstupniho PMML dokumentu
 * @author Tomas Marek
 */
public class SchemaChecker {
    String schemaPath;
    
    /**
     * Kontruktor instance tridy SchemaChecker
     * @param schemaPath cesta ke schematu
     */
    public SchemaChecker(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    /**
     * Trida obsluhujici kontrolu validity dokumentu
     * @param doc vstupni dokument ke kontrole
     * @return [0] - 1/0 (validni/nevalidni), [1] - vypis pripadne chyby a nevalidity
     * @throws IOException
     */
    public String[] validate(String doc) throws IOException{
        String output[] = new String[2];
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        File schemaLocation = new File(schemaPath);
        try {
            Schema schema = factory.newSchema(schemaLocation);
            Validator validator = schema.newValidator();
            InputStream is = new ByteArrayInputStream(doc.getBytes());
            Source source = new StreamSource(is);
            validator.validate(source);
            output[0] = "1";
            output[1] = "";
        } catch (SAXException ex) {
            output[0] = "0";
            output[1] = ex.toString();
        }
        return output;
    }
}