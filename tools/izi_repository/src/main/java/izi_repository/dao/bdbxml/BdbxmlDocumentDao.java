package izi_repository.dao.bdbxml;

import izi_repository.dao.DocumentDao;
import izi_repository.domain.Document;

import org.springframework.stereotype.Repository;


import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlTransaction;

/**
 * Implementation of {@link DocumentDao}.
 * 
 * @author Tomas Marek
 *
 */
@Repository
public class BdbxmlDocumentDao extends AbstractDao implements DocumentDao {

	/*
	 * @{InheritDoc}
	 */
	public Document getDocumentById(String docId) {
		XmlContainer cont = null;
        XmlTransaction trans = null;
        try {
        	cont = xmlManager.openContainer(containerName);
        	trans = xmlManager.createTransaction();
        	
			XmlDocument returnedDocument = cont.getDocument(docId);
			return new Document(returnedDocument.getName(), returnedDocument.getContentAsString());
		} catch (XmlException e) {
			logger.logWarning(this.getClass().toString(), "Getting the document with id \"" + docId + "\" failed!");
			return null;
		} finally {
			commitAndClose(trans, cont);
		}
	}

	/*
	 * @{InheritDoc}
	 */
	public boolean insertDocument(Document document) {
		XmlContainer cont = null;
        XmlTransaction trans = null;
        try {
        	cont = xmlManager.openContainer(containerName);
        	trans = xmlManager.createTransaction();
        	
			cont.putDocument(document.getDocId(), document.getDocBody());
			return true;
		} catch (XmlException e) {
			return false;
		} finally {
			commitAndClose(trans, cont);
		}
	}

	/*
	 * @{InheritDoc}
	 */
	public boolean removeDocument(String docId) {
		XmlContainer cont = null;
        XmlTransaction trans = null;
        try {
        	cont = xmlManager.openContainer(containerName);
        	trans = xmlManager.createTransaction();
        	
			cont.deleteDocument(docId);
			return true;
		} catch (XmlException e) {
			logger.logWarning(this.getClass().toString(), "Removing document with id \"" + docId + "\" failed!");
			return false;
		} finally {
			commitAndClose(trans, cont);
		}
	}
	
}
