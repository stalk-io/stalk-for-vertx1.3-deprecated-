package io.stalk.mod.mail;

import io.stalk.common.api.MailSenderConfig;

import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class MailSender extends BusModBase implements Handler<Message<JsonObject>> {

	private MailSenderConfig moduleConfig;

	private Session 	session;
	private Transport 	transport;

	@Override
	public void start() {
		super.start();

		moduleConfig = new MailSenderConfig(config);

		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", moduleConfig.getHost());
		props.put("mail.smtp.socketFactory.port", Integer.toString(moduleConfig.getPort()));
		if (moduleConfig.isSsl()) {
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		}
		props.put("mail.smtp.socketFactory.fallback", Boolean.toString(false));
		props.put("mail.smtp.auth", Boolean.toString(moduleConfig.isAuth()));
		//props.put("mail.smtp.quitwait", "false");

		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(moduleConfig.getUsername(), moduleConfig.getPassword());
			}
		});
		//session.setDebug(true);

		try {
			transport = session.getTransport();
			transport.connect();
		} catch (MessagingException e) {
			logger.error("Failed to setup mail transport", e);
		}

		eb.registerHandler(moduleConfig.getAddress(), this);

	}

	@Override
	public void stop() {
		try {
			transport.close();
		} catch (MessagingException e) {
			logger.error("Failed to stop mail transport", e);
		}
	}

	@Override
	public void handle(Message<JsonObject> message) {

		String fromEmail 	= moduleConfig.getFromEmail();
		String id 			= message.body.getString("id");
		String toEmail 		= message.body.getString("email");
		String authCode		= message.body.getString("auth");

		if (fromEmail == null) {
			sendError(message, "from address must be specified");
			return;
		}
		InternetAddress fromAddress;
		try {
			fromAddress = new InternetAddress(fromEmail, true);
		} catch (AddressException e) {
			sendError(message, "Invalid from address: " + fromEmail, e);
			return;
		}
		
		if (toEmail == null) {
			sendError(message, "to address must be specified");
			return;
		}
		InternetAddress[] toAddress;
		try {
			toAddress = InternetAddress.parse(toEmail, true);
		} catch (AddressException e) {
			sendError(message, "Invalid to address: " + toEmail, e);
			return;
		}


		if (authCode == null) {
			sendError(message, "an auth code must be specified");
			return;
		}
		
		String subject = moduleConfig.getTitle();
		if (StringUtils.isEmpty(subject)) {
			sendError(message, "subject must be specified");
			return;
		}

		String body = StringUtils.replace(moduleConfig.getContent(), "#LINK_PATH#", id+"/"+authCode);
		if ( StringUtils.isEmpty(body)) {
			sendError(message, "body must be specified");
			return;
		}

		javax.mail.Message msg = new MimeMessage(session);

		try {

			msg.setFrom(fromAddress);
			msg.setRecipients(javax.mail.Message.RecipientType.TO, toAddress);
			msg.setSubject(subject);
			msg.setContent(body, moduleConfig.getContentType());
			msg.setSentDate(new Date());
			transport.send(msg);

			sendOK(message);

		} catch (MessagingException e) {
			e.printStackTrace();
			sendError(message, "Failed to send message", e);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}

