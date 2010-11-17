package as.markon.server;

import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class MailServiceImpl {
	public void sendMail(String subject, String message, List<String> recipients) throws EmailException {
		for (String recipiant : recipients) {
			HtmlEmail mail = new HtmlEmail();
			mail.setHostName("smtp.gmail.com");
			mail.setSmtpPort(587);
			mail.addTo(recipiant);
			mail.setFrom("markon@markon.as");
			mail.setSubject(subject);
			mail.setHtmlMsg(message);
			mail.setTextMsg("Dit mail-program understøtter desværre ikke html-beskeder. Du anbefales at opgradere dit mail-program.\n\n" +
			"Your mail program does not support html-messages. We recommend that you upgrade your program.");
			
			mail.setTLS(true);
			mail.setAuthentication("krdata@gmail.com", "");
			
			mail.send();
		}
	}
}
