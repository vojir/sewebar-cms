package xquerysearch.transformation;

import org.springframework.oxm.castor.CastorMarshaller;

import xquerysearch.domain.result.Result;
import xquerysearch.domain.result.datadescription.ResultDataDescription;
import xquerysearch.mapping.MappingCastor;

/**
 * Transformer used to transform retrieved data description from database.
 * 
 * @author Tomas Marek
 * 
 */
public class ResultDataDescriptionTransformer {

	private static final MappingCastor<ResultDataDescription> mappingCastor = new MappingCastor<ResultDataDescription>();
	
	/**
	 * Default constructor - made private, class provides only static methods
	 */
	private ResultDataDescriptionTransformer() {
	}
	
	/**
	 * Transforms data from DB to {@link Result} object.
	 * 
	 * @param dataDescriptionCastor
	 * @param dataDescription
	 * @return
	 */
	public static ResultDataDescription transform(CastorMarshaller dataDescriptionCastor, final String dataDescription) {
		return mappingCastor.targetToObject(dataDescriptionCastor, dataDescription);
	}
}
