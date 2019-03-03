package com.patho.main.template;

import com.patho.main.model.PDFContainer;
import com.patho.main.util.DateTool;
import com.patho.main.util.helper.TextToLatexConverter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MailTemplate extends AbstractTemplate {

	public PDFContainer attachment;

	private String subject;

	private String finalSubject;

	private String body;

	private String finalBody;

	public MailTemplate() {
	}

	public MailTemplate(MailTemplate document) {
		copyIntoDocument(document);
	}

	public void initilize(InitializeToken... token) {
		HashMap<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < token.length; i++) {
			map.put(token[i].getKey(), token[i].getValue());
		}
		initilize(map);
	}

	public void initilize(HashMap<String, Object> content) {
		initVelocity();

		/* create a context and add data */
		VelocityContext context = new VelocityContext();

		for (Map.Entry<String, Object> entry : content.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		// default date tool
		context.put("date", new DateTool());
		context.put("latexTextConverter", new TextToLatexConverter());

		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();

		Velocity.evaluate(context, writer, "test", subject);

		finalSubject = writer.toString();

		Velocity.evaluate(context, writer, "test", body);

		finalBody = writer.toString();
	}

	public void copyIntoDocument(MailTemplate document) {
		setId(document.getId());
		setName(document.getName());
		setContent(document.getContent());
		setContent2(document.getContent2());
		setType(document.getType());
		setAttributes(document.getAttributes());
		setTemplateName(document.getTemplateName());
		setDefaultOfType(document.isDefaultOfType());
		setTransientContent(document.isTransientContent());
		setBody(document.getBody());
		setSubject(document.getSubject());
	}
	
	public MailType getDocumentType() {
		return MailType.fromString(this.type);
	}

	public PDFContainer getAttachment() {
		return this.attachment;
	}

	public String getSubject() {
		return this.subject;
	}

	public String getFinalSubject() {
		return this.finalSubject;
	}

	public String getBody() {
		return this.body;
	}

	public String getFinalBody() {
		return this.finalBody;
	}

	public void setAttachment(PDFContainer attachment) {
		this.attachment = attachment;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setFinalSubject(String finalSubject) {
		this.finalSubject = finalSubject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setFinalBody(String finalBody) {
		this.finalBody = finalBody;
	}

	public static enum MailType {

		ERROR_MAIL("error.mail"), REQUEST_UNLOCK_MAIL("unlock.mail"), SUCCESS_UNLOCK_MAIL("unlockOk.mail"),
		DIAGNOSIS_REPORT_MAIL("diagnosisReport.mail");

		private final String templateName;

		private MailType(String templateName) {
			this.templateName = templateName;
		}

		public static MailType fromString(String text) {
			for (MailType b : MailType.values()) {
				if (b.name().equalsIgnoreCase(text)) {
					return b;
				}
			}
			throw new IllegalArgumentException("No constant with text " + text + " found");
		}

		public String getTemplateName() {
			return this.templateName;
		}
	}
}