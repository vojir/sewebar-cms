package xquery_servlet;

import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Trida pro zpracovani vstupnich pozadavku a vraceni vysledku
 * @author Tomas Marek
 * @version 1.0.4 (5.2.2011)
 */
public class XQuery_servlet extends HttpServlet {


    final static XMLSettingsReader xmlSettings = new XMLSettingsReader();
    /*
     * 0 - envDir
     * 1 - queryDir
     * 2 - containerName
     * 3 - useTransformation
     * 4 - xsltPath
     * 5 - tempDir
     * 6 - error messages
     */

    final static String[] settings = xmlSettings.readSettings("c:/users/Tomas/Sewebar/dbxml_settings.xml");
    
    //final static String[] settings = xmlSettings.readSettings("/home/marek/dbxml_settings.xml");



    final static String envDir = settings[0];
    final static String queryDir = settings[1];
    final static String containerName = settings[2];
    final static String useTransformation = settings[3];
    final static String xsltPath = settings[4];
    final static String tempDir = settings[5];
    final static String settingsError = settings[6];

    //static FileInputStream xsltTrans = xsltPrepare(new File(xsltPath));

    /*static String envDir = "";
    static String queryDir = "";
    static String containerName = "";
    static String useTransformation = "";
    static String xsltPath = "";
    static String tempDir = "";
    static String settingsError = "";*/

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        /*
         * Nastaveni zpusobu a kodovani vystupu a vstupu,
         * inicializace promennych pro vystup
         */
        response.setContentType("text/xml;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String output = "";
        double time_start = System.currentTimeMillis();

        if (settingsError != null) {
                output += "<error>" + settingsError + "</error>";
        } else {
        try {


        // Vytvoreni spojeni s BDB XML

        Environment env = createEnvironment(envDir, true);
        XmlManagerConfig mconfig = new XmlManagerConfig();
        mconfig.setAllowExternalAccess(true);
        XmlManager mgr = new XmlManager(env, mconfig);


         // Parametr action neni vyplnen => error, jinak naplneni promennych
         // a odeslani ke zpracovani

        QueryHandler qh = new QueryHandler();
        BDBXMLHandler bh = new BDBXMLHandler();
        Tester tester = new Tester();

        if (request.getParameter("action").equals("")){
                output += "<error>Parametr akce neni vyplnen!</error>";
        } else {
                String akce = request.getParameter("action").toString().toLowerCase();
                String promenna = request.getParameter("variable").toString();
                String obsah = request.getParameter("content").toString();

                output += processRequest(akce, promenna, obsah, mgr, qh, bh, tester);
        }

                // Ukonceni spojeni s BDB XML a vycisteni

        mgr.close();
        mgr.delete();

        env.close();
        Environment.remove(env.getHome(), true, EnvironmentConfig.DEFAULT);
        
    }
    catch (Throwable ex) {
        //Logger.getLogger(XQuery_servlet.class.getName()).log(Level.SEVERE, null, ex);
        //output += "<err>" + ex.toString() +"</err>";
        }
    }
        // Vypocet doby zpracovani,
        // vytvoreni a odeslani vystupu
        double time_end = System.currentTimeMillis();
        String cas = Double.toString(((time_end - time_start)));

        if (request.getParameter("action").equals("getDocument")) {
            out.println(output);
        } else {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<result milisecs=\"" + cas + "\">");
            out.println(output);
            out.println("</result>");
        }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "XQuery servlet slouzi ke komunikaci s Berkeley XML DB";
    }// </editor-fold>

    /*
     * Metoda pro vytvoreni spojujiciho prostredi pro BDB XML,
     * jeho nastaveni
     */
    private static Environment createEnvironment(String home, boolean recover)
    throws Throwable {
            EnvironmentConfig config = new EnvironmentConfig();
            config.setTransactional(true);
            config.setAllowCreate(true);
            config.setInitializeCache(true);
            config.setRunRecovery(recover);
            config.setCacheSize(128 * 1024 * 1024); // 128MB cache
            config.setInitializeLocking(true);
            config.setInitializeLogging(true);
            config.setErrorStream(System.err);
            config.setLockDetectMode(LockDetectMode.MINWRITE);
            config.setLogAutoRemove(true);
            config.setLockTimeout(3);
            File f = new File(home);
            return new Environment(f, config);
    }

    /*private static FileInputStream xsltPrepare(File xsltFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xsltFile);
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(XQuery_servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fis;
    }*/

    private static int mapAction (String action){
        /*
         - usequery
         - directquery
         - directquery10
         - addquery
         - getquery
         - deletequery
         - getqueriesnames
         - getdocsnames
         - adddocument
         - adddocumentmultiple
         - getdocument
         - deletedocument
         - addindex
         - completetest
         */
        int returnID = 0;
        if (action.equals("usequery")) returnID = 1; else
        if (action.equals("directquery")) returnID = 2; else
        if (action.equals("directquery10")) returnID = 3; else
        if (action.equals("addquery")) returnID = 4; else
        if (action.equals("getquery")) returnID = 5; else
        if (action.equals("deletequery")) returnID = 6; else
        if (action.equals("getqueriesnames")) returnID = 7; else
        if (action.equals("getdocsnames")) returnID = 8; else
        if (action.equals("adddocument")) returnID = 9; else
        if (action.equals("adddocumentmultiple")) returnID = 10; else
        if (action.equals("getdocument")) returnID = 11; else
        if (action.equals("deletedocument")) returnID = 12; else
        if (action.equals("addindex")) returnID = 13; else
        if (action.equals("completetest")) returnID = 14;
        return returnID;
	}

	
    /**
     * Metoda provadejici rozbor vstupnich promennych,
     * nasledne vola jednotlive metody
     * @param action Nazev akce, ktera se ma provest
     * @param variable Promenna - vetsinou ID (dokumentu/XQuery)
     * @param content Obsah - vetsinou telo (dokumentu, XQuery, index)
     * @param mgr XmlManager
     * @return Sestaveny vystup
     */
    private static String processRequest(String action, String variable, String content, XmlManager mgr, QueryHandler qh, BDBXMLHandler bh, Tester tester){
    	int mappedAction = mapAction(action);
        String output = "";

        switch (mappedAction) {
            case 0: output += "<error>Zadana akce neexistuje</error>"; break;
            case 1: if (content.equals("")) {
                        output += "<error>Neni zadan obsah query</error>";
                    } else {
                        String dotaz = content.toString();
                        String[] message = bh.query(variable, dotaz, 1, mgr, qh, containerName, queryDir);
                        output += message[1].toString();
                    } break;
            case 2: if (content.equals("")) {
                        output += "<error>Query nebyla zadana!</error>";
                    } else {
                        String dotaz = content.toString();
                        String[] message = bh.query("", dotaz, 0, mgr, qh, containerName, queryDir); 
                        output += message[1].toString();
                    } break;
            case 3: if (content.equals("")) {
                        output += "<error>Query nebyla zadana!</error>";
                    } else {
                        String dotaz = content.toString();
                        String message = bh.query_10(dotaz, mgr, qh, containerName, queryDir);
                        output += message.toString();
                    } break;
            case 4: if (content.equals("")) {
                        output += "<error>Neni zadan obsah query</error>";
                    } else {
                        content = content.toString();
                        output += qh.addQuery(content, variable, queryDir);
                    } break;
            case 5: output += "<query>" + qh.getQuery(variable, queryDir)[1].toString() + "</query>"; break;
            case 6: output += qh.deleteQuery(variable, queryDir); break;
            case 7: output += qh.getQueriesNames(queryDir); break;
            case 8: output += bh.getDocsNames(mgr, containerName); break;
            case 9: if (content.equals("")) {
                        output += "<error>Neni zadan obsah dokumentu</error>";
                    } else {
                        content = content.toString();
                        output += bh.indexDocument(content, variable, mgr, containerName, useTransformation, xsltPath);
                    } break;
            case 10: output += bh.indexDocumentMultiple(content, mgr, containerName, useTransformation, xsltPath); break;
            case 11: if (variable == null){
                        output += "<error>Neni zadan nazev dokumentu</error>";
                    } else {
                        output += bh.getDocument(variable, mgr, containerName);
                    } break;
            case 12: output += bh.removeDocument(variable, mgr, containerName); break;
            case 13: if (content.equals("")){
                        output += "<error>Index nebyl zadan!</error>";
                    } else {
                        String dotaz = content.toString();
                        output += bh.addIndex(dotaz, mgr, containerName);
                    } break;
            case 14: output += tester.runTest(qh, bh, mgr, envDir, queryDir, containerName, useTransformation, xsltPath, tempDir, settingsError); break;
            default: output += "<error>Zadana akce neexistuje</error>"; break;
        }
	return (output);
    }
}

/*
 * if (action.equals("getdocsnames")) {
                    output += bh.getDocsNames(mgr, containerName);
		} else if (action.equals("getqueriesnames")) {
                    output += qh.getQueriesNames(queryDir);
                } else if (action.equals("completetest")) {
                    output += tester.runTest(qh, bh, mgr, envDir, queryDir, containerName, useTransformation, xsltPath, tempDir, settingsError);
                } else if (action.equals("directquery")) {
			if (content.equals("")) {
                            output += "<error>Query nebyla zadana!</error>";
			} else {
                            String dotaz = content.toString();
                            String[] message = bh.query("", dotaz, 0, mgr, qh, containerName, queryDir);
                            output += message[1].toString();
			}
		} else if (action.equals("directquery10")) {
			if (content.equals("")) {
				output += "<error>Query nebyla zadana!</error>";
			} else {
				String dotaz = content.toString();
				String message = query_10(dotaz, mgr);
                                output += message.toString();
			}
		} else if (action.equals("adddocumentmultiple")) {
                    output += bh.indexDocumentMultiple(content, mgr, containerName, useTransformation, xsltPath);
                } else if (action.equals("addindex")) {
                    if (content.equals("")){
                        output += "<error>Index nebyl zadan!</error>";
                    } else {
                        String dotaz = content.toString();
                        output += bh.addIndex(dotaz, mgr, containerName);
                    }
            } else if (action.equals("adddocument")) {
	    		if (content.equals("")) {
	    			output += "<error>Neni zadan obsah dokumentu</error>";
	    		} else {
	    			content = content.toString();
	    			output += bh.indexDocument(content, variable, mgr, containerName, useTransformation, xsltPath);
	    		}
	    	} else if (variable.equals("")) {
				output += "<error>Parametr ID neni vyplnen!</error>";
			}
		else {
			variable = variable.toString();

			//action = usequery
	    	if (action.equals("usequery")) {
	    		if (content.equals("")) {
	    			output += "<error>Neni zadan obsah query</error>";
	    		} else {
	    				String dotaz = content.toString();
                        String[] message = bh.query(variable, dotaz, 1, mgr, qh, containerName, queryDir);
                        output += message[1].toString();
				}

	    	//action = getquery
	    	} else if (action.equals("getquery")) {
	    		output += "<query>" + qh.getQuery(variable, queryDir)[1].toString() + "</query>";
	    	} else if (action.equals("addquery")) {
	    		if (content.equals("")) {
	    			output += "<error>Neni zadan obsah query</error>";
	    		} else {
	    			content = content.toString();
	    			output += qh.addQuery(content, variable, queryDir);
	    		}

	    	//action = deletequery
	    	} else if (action.equals("deletequery")){
	    		output += qh.deleteQuery(variable, queryDir);

	    	//action = moredoc
	    	} else if (action.equals("moredoc")) {
	    		if (content.equals("")) {
	    			output_temp += "<error>Neni zadan obsah</error>";
	    		} else {
	    			content = content.toString();
                    String[] message = bh.moreDocuments(content, variable, mgr);
	    			output_temp += message[1].toString();
	    		}
	    	//action = getdocument
	    	} else if (action.equals("getdocument")) {
	    		if (variable == null){
	    			output += "<error>Neni zadan nazev dokumentu</error>";
	    		} else {
	    			output += bh.getDocument(variable, mgr, containerName);
	    		}

	    	//action = deletedocument
	    	} else if (action.equals("deletedocument")) {
	    		output += bh.removeDocument(variable, mgr, containerName);

	    	//error - zadana action neexistuje
	    	} else {
	    		output += "<error>Zadana akce neexistuje</error>";
	    		}
    	}
 */