package ca.sheridancollege.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.util.List;
import ca.sheridancollege.beans.Orders;
import ca.sheridancollege.beans.Product;
@Service
public class MailClient {

	 private JavaMailSender mailSender;
	 
	    @Autowired
	    public MailClient(JavaMailSender mailSender) {
	        this.mailSender = mailSender;
	    }
	    @Autowired
	    private MailContentBuilder mailContentBuilder;
	 
	    public void prepareAndSend(String from,String recipient, String subject, String text) {
	    	MimeMessagePreparator messagePreparator = mimeMessage -> {
		        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
		        messageHelper.setFrom(from);
		        messageHelper.setTo(recipient);
		        messageHelper.setSubject(subject);
		     
		        String content = mailContentBuilder.build(text,subject);
		      
		        messageHelper.setText(content, true);
		    };
		    try {
		        mailSender.send(messagePreparator);
		    }catch(MailException e) {
		    	System.out.println(e.getMessage());
		    }
	    }
	    public void orderConfirmation(String from,String recipient, String subject, String text, Orders order, List<Product> cartItems) {
	    	MimeMessagePreparator messagePreparator = mimeMessage -> {
		        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
		        messageHelper.setFrom(from);
		        messageHelper.setTo(recipient);
		        messageHelper.setSubject(subject);
		     
		        String content = mailContentBuilder.confirmOrder(text,subject, order, cartItems);
		      
		        messageHelper.setText(content, true);
		    };
		    try {
		        mailSender.send(messagePreparator);
		    }catch(MailException e) {
		    	System.out.println(e.getMessage());
		    }
	    }
	 
}
