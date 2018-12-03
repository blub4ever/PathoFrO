package com.patho.main.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.patho.main.common.DateFormat;
import com.patho.main.config.PathoConfig;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.PDFRepository;
import com.patho.main.util.DataBaseConverter;
import com.patho.main.util.FileMakerImporter;
import com.patho.main.util.helper.TimeUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "session")
@Slf4j
public class MainHandlerAction {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFRepository pdfRepository;

	@Getter
	@Setter
	private String number;

	@Autowired
	@Lazy
	private UserHandlerAction userHandlerAction;

	@Autowired
	protected ResourceBundle resourceBundle;

	/********************************************************
	 * Navigation
	 ********************************************************/

	@Getter
	@Setter
	private List<FacesMessage> queueGrowlMessages = new ArrayList<FacesMessage>();


	public void test() {
//		DataBaseConverter b = new DataBaseConverter();
//		b.start();

		try {
			System.out.println(getBaseURL(FacesContext.getCurrentInstance()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/********************************************************
	 * Date
	 ********************************************************/

	/**
	 * Takes a long timestamp and returns a formatted date in standard format.
	 * 
	 * @param date
	 * @return
	 */
	public String date(long date) {
		return date(new Date(date), DateFormat.GERMAN_DATE);
	}

	/**
	 * Takes a date and returns a formatted date in standard format.
	 * 
	 * @param date
	 * @return
	 */
	public String date(Date date) {
		return date(date, DateFormat.GERMAN_DATE);
	}

	/**
	 * Takes a long timestamp and a format string and returns a formatted date.
	 * 
	 * @param date
	 * @return
	 */
	public String date(long date, String format) {
		return date(new Date(date), format);
	}

	/**
	 * Takes a dateFormat an returns a formated string.
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public String date(long date, DateFormat format) {
		return date(new Date(date), format.getDateFormat());
	}

	/**
	 * Takes a dateFormat an returns a formated string.
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public String date(Date date, DateFormat format) {
		return date(date, format.getDateFormat());
	}

	/**
	 * Takes a date and a format string and returns a formatted date.
	 * 
	 * @param date
	 * @return
	 */
	public String date(Date date, String format) {
		return TimeUtil.formatDate(date, format);
	}

	/********************************************************
	 * Date
	 ********************************************************/

	public void test123() {
		taskExecutor.execute(new Thread() {
			public void run() {
				PDFContainer returnPDF = new PDFContainer();
				returnPDF.setName("Hallo");
				pdfRepository.save(returnPDF);
			}
		});
	}

	public void importCSV() {
		FileMakerImporter f = new FileMakerImporter();
		try {
			f.importFilemaker(number);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Determines the Base URL, e.g., {@literal http://localhost:8080/myApplication}
	 * from the {@link FacesContext}.
	 * </p>
	 *
	 * @param facesContext The {@link FacesContext} to examine.
	 * @return the base URL.
	 * @throws MalformedURLException if an exception occurs during parsing of the
	 *                               URL.
	 * @since 1.3
	 */
	public String getBaseURL(final FacesContext facesContext) throws MalformedURLException {
		return getBaseURL(facesContext.getExternalContext());
	}

	/**
	 * <p>
	 * Determines the Base URL, e.g., {@literal http://localhost:8080/myApplication}
	 * from the {@link ExternalContext}.
	 * </p>
	 *
	 * @param externalContext The {@link ExternalContext} to examine.
	 * @return the base URL.
	 * @throws MalformedURLException if an exception occurs during parsing of the
	 *                               URL.
	 * @since 1.3
	 */
	public String getBaseURL(final ExternalContext externalContext) throws MalformedURLException {
		return getBaseURL((HttpServletRequest) externalContext.getRequest());
	}

	/**
	 * <p>
	 * Determines the Base URL, e.g., {@literal http://localhost:8080/myApplication}
	 * from the {@link HttpServletRequest}.
	 * </p>
	 *
	 * @param request The {@link HttpServletRequest} to examine.
	 * @return the base URL.
	 * @throws MalformedURLException if an exception occurs during parsing of the
	 *                               URL.
	 * @see URL
	 * @since 1.3
	 */
	public String getBaseURL(final HttpServletRequest request) throws MalformedURLException {
		return new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath())
				.toString();
	}

}
