package com.baidu.stock.process.monitor.mvc;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author dengjianli
 *
 */
@Controller
public class HQSummaryController {
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String summary(Map<String, Object> model) {
		return "summary";
	}
}