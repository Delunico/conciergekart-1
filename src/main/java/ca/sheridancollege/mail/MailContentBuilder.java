package ca.sheridancollege.mail;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import ca.sheridancollege.beans.Orders;
import ca.sheridancollege.beans.Product;

@Service
public class MailContentBuilder {

	private TemplateEngine templateEngine;
	 
    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
 
    public String build(String message,String subject) {
    	Context context = new Context();
	      if(subject.contains("Recovery")) {
	    	  context.setVariable("recovery", true);
	      }
	      if(subject.contains("message")){
	    	  context.setVariable("contactus", true);
	      }
        
        context.setVariable("message", message);
        context.setVariable("subject", subject);
        UUID uuid = UUID.randomUUID();
        context.setVariable("value", uuid);
      
        return templateEngine.process("mailTemplate", context);
    }
    
    
    
    public String confirmOrder(String message,String subject, Orders order, List<Product> cartItems) {
    	Context context = new Context();
	    if(subject.contains("New")) {
	    	context.setVariable("new_order",true);
	    }
	    context.setVariable("order", order);
	    context.setVariable("cartItems", cartItems);
        context.setVariable("message", message);
        context.setVariable("subject", subject);
        UUID uuid = UUID.randomUUID();
        context.setVariable("value", uuid);
      
        return templateEngine.process("mailTemplate", context);
    }
 
}

