package xquerysearch.dao;

import java.util.List;

/**
 * DAO used for get additional informations from database.
 * Informations like all documents names.
 * 
 * @author Tomas Marek
 *
 */
public interface HelperDao {
	
	public List<String> getAllDocumentsNames();
	
	public List<String[]> getAllIndexes();

}
