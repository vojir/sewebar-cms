package xquery_servlet;

import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlIndexDeclaration;
import com.sleepycat.dbxml.XmlIndexSpecification;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

/**
 * Trida pro ovladani a komunikaci s Berkeley XML DB
 * @author Tomas Marek
 */
public class BDBXMLHandler {
    XmlManager mgr;
    QueryHandler qh;
    String containerName;
    String useTransformation;
    String xsltPath;
    Pattern replaceMask = Pattern.compile("[|!@$^* \\//\"\',?ˇ´<>¨;¤×÷§]");
    String replaceBy = "_";
    
    /**
     * Konstruktor
     * @param mgr instance XmlManager
     * @param qh instance tridy QueryHandler
     * @param containerName nazev pouzivaneho kontejneru
     * @param useTransformation pouzit transformaci - true/false
     * @param xsltPath cesta k souboru s xslt transformaci
     */
    public BDBXMLHandler(XmlManager mgr, QueryHandler qh, String containerName, String useTransformation, String xsltPath) {
        this.mgr = mgr;
        this.qh = qh;
        this.containerName = containerName;
        this.useTransformation = useTransformation;
        this.xsltPath = xsltPath;
    }

    /**
     * Metoda pro vymazani dokumentu z XML DB
     * @param id ID dokumentu v DB
     * @return Zprava - splneno/chyba
     */
    public String removeDocument (String id){
        String output = "";
        try {

            XmlContainer cont = mgr.openContainer(containerName);
            XmlTransaction txn = mgr.createTransaction();

            if (cont.getDocument(id) != null) {
                cont.deleteDocument(id);
                output += "<message>Dokument " + id + " smazan!</message>";
            } else {
                output += "<error>Dokument nenalezen!</error>";
            }
            txn.commit();
            closeContainer(cont);
        } catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }

        return output;
    }

    /**
     * Metoda pro zobrazeni dokumentu z XML DB
     * @param id ID dokumentu v DB
     * @return Zobrazeni dokumentu/chyba
     */
    public String getDocument(String id){
        String output = "";
        try {
            XmlContainer cont = mgr.openContainer(containerName);
            XmlTransaction txn = mgr.createTransaction();

            if (cont.getDocument(id) != null) {
                XmlDocument doc = cont.getDocument(id);
                output += doc.getContentAsString();
            } else {
                output += "<error>Dokument nenalezen!</error>";
            }
            txn.commit();
            closeContainer(cont);
        } catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }
        return output;
    }

    /**
     * Cyklycke dotazovani - 10x za sebou stejny dotaz na XML DB (pouzito pri testovani)
     * @param search XQuery dotaz
     * @return cas a vysledky dotazovani 
     */
    public String query_10(String search) {
        String output = "";
        String output_temp = "";
        for (int i=0; i<10; i++){
            output += "<pokus cislo=\""+ i +"\">";
            double time_start = System.currentTimeMillis();
            output_temp = query("", search, 0);
            output += "<time>"+ (System.currentTimeMillis() - time_start) +"</time>";
            if (i == 9){
                output += output_temp;
            }
            output += "</pokus>";
        }
        return output;
    }

   /**
     * Metoda slouzici k vyhledavani v BDB XML pomoci XQuery
     * Moznosti zadani - prima XQuery/pouziti ulozene XQuery a pridani vstupniho dotazu
     * @param id ID ulozene XQuery
     * @param search vstupni dotaz pro XQuery
     * @param type typ pouzite XQuery - 0 pro primou, 1 pro ulozenou
    */
    public String query(String id, String search, int type){
        String output = "";
        int chyba = 0;
        
        try {
            XmlContainer cont = mgr.openContainer(containerName);
            String query = "";
            if (type == 0) {
                query = search;
            } else {
                    if (qh.getQuery(id)[0].toString().equals("1")) {
                        output = qh.getQuery(id)[1].toString();
                        chyba = 1;
                    } else {
                        query = qh.getQuery(id)[1].toString();
                        query += "\nlet $zadani := " + search
                                + "\nreturn local:mainFunction($zadani)";
                    }
            }
            if (chyba != 1) {
            	query = deleteDeclaration(query);
	            XmlQueryContext qc = mgr.createQueryContext();
	            XmlTransaction txn = mgr.createTransaction();
	            XmlResults res = mgr.query(query, qc);
	
	            if (res != null) {
	            // Process results -- just print them
	                    XmlValue value = new XmlValue();
	                    while ((value = res.next()) != null) {
	                        output += (value.asString());
	                    }
	            } else {
	                output = "<error>Zadny vysledek</error>";
	            }
	            txn.commit();
	            res.delete();
	            closeContainer(cont);
            }
	        } catch (XmlException e) {
	                output += "<error>"+e.toString()+"</error>";
	        } catch (Throwable e) {
	                output += "<error>"+e.toString()+"</error>";
	        }
        return output;
    }

    /**
     * !!! NEPOUZITO !!!
     * Metoda pro vlozeni vice dokumentu najednou,
     * dokumenty rozdeleny sekvenci znaku ;;;NEXTPMML;;;
     * @param docs vsechny tela vkladanych dokumentu oddelene danou sekvenci znaku
     * @param names vsechny ID vkladanych dokumentu oddelene danou sekvenci znaku
     * @return Zprava pro kazdy dokument - vlozeno/chyba
     */

    /*public String[] moreDocuments(String docs, String names){
    String output[] = new String[2];
        output[1] = "";
        long cas_zacatek = System.currentTimeMillis();
        long cas_konec = 0;
        String[] dokumenty = docs.split(";;;NEXTPMML;;;");
        String[] jmena = names.split(";;;NEXTPMML;;;");
        if (dokumenty.length != jmena.length){
                output[1] = "<error>Nastala chyba!!!</error>";
        } else {
                for (int i = 0; i < dokumenty.length; i++){
                    output[1] += "\n" + indexDocument(dokumenty[i], jmena[i], mgr);
                }
        cas_konec = System.currentTimeMillis();
        output[0] = "" + ((cas_konec - cas_zacatek)/1000);
        }
        return output;
    }*/

    /**
     * Metoda pro ziskani nazvu dokumentu ulozenych v XML DB
     */
    public String getDocsNames(){
        String output = "";
        try {
            String query = "let $docs := for $x in collection(\""+containerName+"\") return $x"
                    + "\nreturn"
                    + "\n<docs count=\"{count($docs)}\">{for $a in $docs"
                    + "\norder by dbxml:metadata(\"dbxml:name\", $a)"
                    + "\nreturn <doc>{dbxml:metadata(\"dbxml:name\", $a)}</doc>}</docs>";

            XmlContainer cont = mgr.openContainer(containerName);
            XmlQueryContext qc = mgr.createQueryContext();
            XmlTransaction txn = mgr.createTransaction();
            XmlResults res = mgr.query(query, qc);

            XmlValue value = new XmlValue();
            while ((value = res.next()) != null) {
                output += value.asString();
            }
            txn.commit();
            closeContainer(cont);
        } catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda pro vlozeni dokumentu do XML DB
     * @param document telo dokumentu (String)
     * @param id ID dokumentu
     * @return Zprava - ulozeno/chyba
     */
    public String indexDocument(String document, String id){

        String output = "";
        String xml_doc = "";
        
        try {
            if (useTransformation.equals("true")) {
        
            File xsltFile = new File(xsltPath);
            XSLTTransformer xslt = new XSLTTransformer();
            xml_doc = xslt.xsltTransformation(document, xsltFile);
            //output += "<xslt>" + xslt_output + "</xslt>";
            //xmlFile.delete();
            
        } else {
                xml_doc = document;
        }

            XmlContainer cont = mgr.openContainer(containerName);
            XmlTransaction txn = mgr.createTransaction();
            
            id = id.replaceAll(replaceMask.toString(), replaceBy);
            
            cont.putDocument(id, xml_doc);
            output += "<message>Dokument " + id + " vlozen</message>";

            txn.commit();
            closeContainer(cont);
        } catch (XmlException e) {
                output += "<error>"+e.toString()+"</error>";
        } catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda pro vlozeni dokumentu do XML DB
     * @param document telo dokumentu (File)
     * @param id nazev dokumentu
     * @return zprava - ulozeno/chyba
     */
    public String indexDocument(File document, String id){
        String xml_doc = "";
        String output = "";
        long act_time_long = System.currentTimeMillis();
        //String act_time = Long.toString(act_time_long);
        try {
            if (useTransformation.equals("true")) {
            File xsltFile = new File(xsltPath);

            XSLTTransformer xslt = new XSLTTransformer();

            xml_doc = xslt.xsltTransformation(document, xsltFile);
            output += "<xslt_time>" + (System.currentTimeMillis() - act_time_long) + "</xslt_time>";
            //output += "<xslt>" + xslt_output + "</xslt>";
            //xmlFile.delete();
        } else {
            FileReader rdr = null;
            BufferedReader out = null;
            rdr = new FileReader(document);
            out = new BufferedReader(rdr);
            String radek = out.readLine();
            while (radek != null){
                xml_doc += radek + "\n";
                radek = out.readLine();
            }
        }

            XmlContainer cont = mgr.openContainer(containerName);
            XmlTransaction txn = mgr.createTransaction();
            
            id = id.replaceAll(replaceMask.toString(), replaceBy);

            cont.putDocument(id, xml_doc);
            output += "<message>Dokument " + id + " vlozen</message>";
            output += "<doc_time>" + (System.currentTimeMillis() - act_time_long) + "</doc_time>";
            txn.commit();
            closeContainer(cont);
            } catch (XmlException e) {
                output += "<error>"+e.toString()+"</error>";
        } catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda pro nahrani vice dokumentu ze slozky
     * @param folder slozka, ze ktere se maji soubory nahrat
     * @return zprava o ulozeni / chyba
     */
    public String indexDocumentMultiple (String folder) {
        String output = "";
        File uploadFolder = new File(folder);
        File uploadFiles[] = uploadFolder.listFiles();
        
        for(int i = 0; i < uploadFiles.length; i++){
            output += indexDocument(uploadFiles[i], uploadFiles[i].getName());
        }        
        return output;
    }

    /**
     * Metoda pro pridani indexu XML DB
     * @param index zadani indexu - namespace;node;index type
     * @return zprava o pridani indexu / chybe
     */
    public String addIndex(String index) {
        String output = "";

        try {
            XmlContainer cont = mgr.openContainer(containerName);

            XmlTransaction txn = mgr.createTransaction();
            String[] indexPole = index.split(";");
            if (indexPole.length == 3) {
                XmlIndexSpecification indexSpec = cont.getIndexSpecification();
                indexSpec.addIndex(indexPole[0], indexPole[1], indexPole[2]);
                cont.setIndexSpecification(indexSpec);
                output = "<message>Index " + index + " pridan</message>";

                txn.commit();
                indexSpec.delete();
                closeContainer(cont);
            } else {
                output = "<error>Spatne zadany index</error>";
            }
        } catch (XmlException e) {
                output += "<error>"+e.toString()+"</error>";
        }catch (Throwable e) {
                output += "<error>"+e.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda zajistujici smazani indexu
     * @param index zadani indexu - namespace;node;index type
     * @return zprava o smazani indexu / chybe
     */
    public String delIndex (String index){
        String output = "";

        try {
            XmlContainer cont = mgr.openContainer(containerName);
            XmlTransaction txn = mgr.createTransaction();
            String[] indexPole = index.split(";");

            if (indexPole.length == 3) {
                XmlIndexSpecification indexSpec = cont.getIndexSpecification();
                indexSpec.deleteIndex(indexPole[0], indexPole[1], indexPole[2]);
                cont.setIndexSpecification(indexSpec);
                output = "<message>Index " + index + " odebran</message>";

                txn.commit();
                indexSpec.delete();
                closeContainer(cont);
            } else {
                output = "<error>Spatne zadany index</error>";
            }
        } catch (XmlException ex) {
            //Logger.getLogger(BDBXMLHandler.class.getName()).log(Level.SEVERE, null, ex);
            output += "<error>"+ex.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda pro zobrazeni indexu v XML DB
     * @return vypis pouzivanych indexu v XML DB
     */
    public String listIndex() {
        String output = "";
        String outputEnd = "";
        try {
            XmlContainer cont = mgr.openContainer(containerName);
            XmlIndexSpecification indexSpec = cont.getIndexSpecification();

            int count = 0;
            XmlIndexDeclaration indexDecl = null;
            while ((indexDecl = (indexSpec.next())) != null) {
                outputEnd += "<index>"
                            + "<nodeName>" + indexDecl.name + "</nodeName>"
                            + "<indexType>" + indexDecl.index + "</indexType>"
                        + "</index>";
                count++;
            }
            output += "<indexCount>" + count + "</indexCount>" + outputEnd;
            indexSpec.delete();
            closeContainer(cont);
        } catch (XmlException ex) {
            //Logger.getLogger(BDBXMLHandler.class.getName()).log(Level.SEVERE, null, ex);
            output += "<error>"+ex.toString()+"</error>";
        }
        return output;
    }

    /**
     * Metoda pro vytvoreni DataDescription dat ulozenych v XML DB
     * @return DataDescription
     */
    public String getDataDescription(){
        String output = "";
        String query = "<dd:DataDescription xmlns:dd=\"http://keg.vse.cz/ns/datadescription0_1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:pmml=\"http://www.dmg.org/PMML-4_0\" xsi:schemaLocation=\"http://keg.vse.cz/ns/datadescription0_1 http://sewebar.vse.cz/schemas/DataDescription0_1.xsd\">"
        	+ "<Dictionary sourceSubType=\"DataDictionary\" sourceType = \"PMML\" default=\"true\">{"
            + "\nfor $field in distinct-values(collection(\"" + containerName + "\")/PMML/DataDescription/DataField/@name/string()) "
    		+ "\nlet $values :=  for $value in distinct-values(collection(\"" + containerName + "\")/PMML/DataDescription/DataField[@name = $field and @type != \"continuous\"]/Category/text())"
			+ "\nreturn <Category>{$value}</Category>"
			+ "\nlet $leftMargin := for $LM in distinct-values(collection(\"" + containerName + "\")/PMML/DataDescription/DataField[@name = $field and @type = \"continuous\"]/Interval/@leftMargin)"
			+ "\nreturn min($LM)"
			+ "\nlet $rightMargin := for $RM in distinct-values(collection(\"" + containerName + "\")/PMML/DataDescription/DataField[@name = $field and @type = \"continuous\"]/Interval/@rightMargin)"
			+ "\nreturn max($RM)"
			+ "\nlet $int := if (count($leftMargin) > 0 and count($leftMargin) > 0) then <Interval leftMargin = \"{$leftMargin}\" rightMargin = \"{$rightMargin}\" closure = \"closedClosed\"/> else ()"
			+ "\nlet $intCats := distinct-values(for $DF in collection(\"" + containerName + "\")/PMML/DataDescription/DataField[@name = $field and @type = \"continuous\"]"
			+ "\nwhere max(count($DF/Interval/Category))"
			+ "\nreturn $DF/Interval/Category)"
            + "\nreturn"
            + "\n<Field name=\"{$field}\">"
            + "\n{$values union $int}"
            + "\n{for $IC in $intCats return <Category>{$IC}</Category>}"
            + "\n</Field>}</Dictionary></dd:DataDescription>";
        
        /*String query =
                "<DataDescription><Dictionary sourceSubType=\"DataDictionary\" sourceType = \"PMML\" default=\"true\">{"
                + "\nfor $field in distinct-values(collection(\"" + containerName + "\")/PMML/fieldValuesSet/Field/@name/string())"
                + "\nlet $values :=  for $value in distinct-values(collection(\"" + containerName + "\")/PMML/fieldValuesSet/Field[@name = $field and @type != \"continuous\"]/fieldValue/text())"
                + "\nreturn <Category>{$value}</Category>"
                + "\nlet $ints_from := for $int in distinct-values(collection(\"" + containerName + "\")/PMML/fieldValuesSet/Field[@name = $field and @type = \"continuous\"]/fieldValue[1]/@from)"
                + "\nreturn $int"
                + "\nlet $ints_to := for $int in distinct-values(collection(\"" + containerName + "\")/PMML/fieldValuesSet/Field[@name = $field and @type = \"continuous\"]/fieldValue[last()]/@to)"
                + "\nreturn $int"
                + "\nlet $ints := if(count($ints_from) > 0 and count($ints_to) > 0) then <Interval closure=\"\" leftMargin=\"{$ints_from}\" rightMargin=\"{$ints_to}\"/> else ()"
                + "\nreturn"
                + "\n<Field name=\"{$field}\">"
                + "\n{$values union $ints}"
                + "\n</Field>}</Dictionary></DataDescription>";*/

        try {
            XmlContainer cont = mgr.openContainer(containerName);
            XmlQueryContext qc = mgr.createQueryContext();
            XmlTransaction txn = mgr.createTransaction();
            XmlResults res = mgr.query(query, qc);

            XmlValue value = new XmlValue();
            while ((value = res.next()) != null) {
                output += value.asString();
            }
            txn.commit();
            res.delete();
            closeContainer(cont);
            } catch (XmlException ex) {
                //Logger.getLogger(BDBXMLHandler.class.getName()).log(Level.SEVERE, null, ex);
                output += "<error>"+ex.toString()+"</error>";
            }
        return output;
    }

    /**
     * Metoda zajistujici odstraneni XML deklarace z XQuery dotazu
     * @param query vstupni XQuery dotaz
     * @return vraceny dotaz bez XML deklarace / chyba
     */
    private String deleteDeclaration(String query) {
        String output = "";
        String splitXMLBegin[] = query.split("([<][?][x][m][l])|([<][?][o][x][y][g][e][n])");
        if (splitXMLBegin.length == 1) {
            output = query;
        } else {
            for (int i = 0; i <= (splitXMLBegin.length - 1); i++) {
                if (i == 0) {
                    output += splitXMLBegin[i];
                } else {
                    String splitXMLEnd[] = splitXMLBegin[i].split("[?][>]");
                    if (splitXMLEnd.length > 1) {
                        String splitXMLBack = splitXMLEnd[1];
                        output += splitXMLBack;
                    }
                }
            }
        }
        return output;
    }

    /**
     * Metoda zajistujici uzavreni pouzivaneho kontejneru
     * @param cont instance XmlContainer
     */
    private void closeContainer (XmlContainer cont) {
        if (cont != null) {
            try {
                cont.close();
            } catch (XmlException ex) {
            }
        }
    }
}