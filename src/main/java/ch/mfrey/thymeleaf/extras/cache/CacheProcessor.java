package ch.mfrey.thymeleaf.extras.cache;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

public class CacheProcessor extends AbstractAttrProcessor {
	public static final Logger log = LoggerFactory.getLogger(CacheProcessor.class);

	public CacheProcessor() {
		super("name");
	}

	@Override
	protected ProcessorResult processAttribute(Arguments arguments, Element element, String attributeName) {
		log.debug("Checking Cache");
		final String attributeValue = element.getAttributeValue(attributeName);

		final Configuration configuration = arguments.getConfiguration();
		final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);

		final IStandardExpression expression = expressionParser.parseExpression(configuration, arguments, attributeValue);

		final Object result = expression.execute(configuration, arguments);
		if (result == null || result == "") {
			log.debug("Cache name not resolvable: {}", attributeValue);
			return ProcessorResult.OK;
		}
		
		String cacheName = result.toString();
		Object object = arguments.getTemplateEngine().getCacheManager().getFragmentCache()
				.get(CacheDialect.CACHE_PREFIX + cacheName);
		element.removeAttribute(attributeName);
		if (object != null) {
			log.debug("Cache found. Replacing");
			@SuppressWarnings("unchecked")
			List<Node> children = (List<Node>) object;
			element.clearChildren();
			element.setChildren(children);
		} else {
			log.debug("Cache not found. Adding add element");
			Element cacheDiv = new Element("div");
			cacheDiv.setAttribute("cache:add", cacheName);
			element.addChild(cacheDiv);
		}
		element.setRecomputeProcessorsImmediately(true);
		return ProcessorResult.OK;
	}

	@Override
	public int getPrecedence() {
		return 10;
	}

}
