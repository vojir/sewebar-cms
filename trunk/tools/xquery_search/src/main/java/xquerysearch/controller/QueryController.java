package xquerysearch.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import xquerysearch.domain.Query;
import xquerysearch.domain.Result;
import xquerysearch.service.QueryService;

/**
 * Controller for querying.
 * 
 * @author Tomas Marek
 *
 */
@Controller
public class QueryController extends AbstractController {
	
	@Autowired
	private QueryService queryService;
	
	// TODO rename action in jsp
	@RequestMapping(params = "action=useQuery", method = RequestMethod.POST)
	public ModelAndView queryForResult(@RequestParam String content, HttpServletRequest request, HttpServletResponse response) {
		if (content.isEmpty()) {
			addResponseContent("<error>Query content has to be entered!</error>", response);
			return null;
		}
		Query query = new Query(content);
		List<Result> results = queryService.getResults(query);
		StringBuffer responseMessage = new StringBuffer();
		for (Result result : results) {
			responseMessage.append(result.getResultBody());
		}
		addResponseContent(responseMessage.toString(), response);
		return null;
	}

}
