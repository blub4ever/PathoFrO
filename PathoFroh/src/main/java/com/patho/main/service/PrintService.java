package com.patho.main.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.json.JsonHandler;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.ClinicPrinterDummy;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.printer.RoomContainer;

import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
@Getter
@Setter
@ConfigurationProperties(prefix = "patho.printer")
public class PrintService extends AbstractService {

	private CupsPrinterHandler cupsPrinter;

	private LablePrinterHandler lablePrinter;

	@PostConstruct
	public void initilizePrinters() {
		getCupsPrinter().initialize();
	}
	
	@Getter
	@Setter
	public static class CupsPrinterHandler {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		private String host;

		private int port;

		private String testPage;

		private String printerForRoom;

		/**
		 * List of clinicla pritners
		 */
		private List<ClinicPrinter> printer;

		/**
		 * Transformer for printerList
		 */
		private DefaultTransformer<ClinicPrinter> printerTransformer;

		public void initialize() {
			setPrinter(loadCupsPrinters(host, port));
			setPrinterTransformer(new DefaultTransformer<ClinicPrinter>(getPrinter()));
		}

		private List<ClinicPrinter> loadCupsPrinters(String host, int port) {
			ArrayList<ClinicPrinter> result = new ArrayList<ClinicPrinter>();
			CupsClient cupsClient;

			try {
				cupsClient = new CupsClient(host, port);
				List<CupsPrinter> cupsPrinter = cupsClient.getPrinters();
				// transformin into clinicprinters
				for (CupsPrinter p : cupsPrinter) {
					result.add(new ClinicPrinter(p));
				}

			} catch (Exception e) {
				logger.error("Retriving printers failed" + e);
				result.add(0, new ClinicPrinterDummy());
				e.printStackTrace();
			}

			return result;
		}

		/**
		 * Searches the loaded clinical printers for the given printer. If found the
		 * printer from the global list will be returned. This will prevent problems if
		 * something has changed.
		 * 
		 * If no printer was found the first printer will be returned (DummyPrinter)
		 * 
		 * @param clinicPrinter
		 * @return
		 */
		public ClinicPrinter findPrinterByID(long id) {
			for (ClinicPrinter printer : getPrinter()) {
				if (printer.getId() == id)
					return printer;
			}

			logger.debug("Returning dummy printer");
			return getPrinter().get(0);
		}

		/**
		 * Loads a printer from the room in association with the current ip
		 * 
		 * @return
		 */
		public ClinicPrinter getCupsPrinterForRoom() {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			String ip = getRemoteAddress(request);

			if (ip != null) {
				String printerToRoomJson = (new JsonHandler()).requestJsonData(printerForRoom.replace("$ip", ip));

				List<RoomContainer> container = RoomContainer.factory(printerToRoomJson);

				if (container.size() > 0) {
					RoomContainer firstConatiner = container.get(0);

					for (ClinicPrinter printer : getPrinter()) {
						if (HistoUtil.isNotNullOrEmpty(firstConatiner.getPrinter())
								&& firstConatiner.getPrinter().equals(printer.getDeviceUri())) {
							logger.debug("Printer found for room " + ip + "; printer = " + printer.getName());
							return printer;
						}
					}
				}

			}

			return null;

		}

		/**
		 * Gets the remote address from a HttpServletRequest object. It prefers the
		 * `X-Forwarded-For` header, as this is the recommended way to do it (user may
		 * be behind one or more proxies).
		 *
		 * Taken from https://stackoverflow.com/a/38468051/778272
		 *
		 * @param request
		 *            - the request object where to get the remote address from
		 * @return a string corresponding to the IP address of the remote machine
		 */
		public static String getRemoteAddress(HttpServletRequest request) {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress != null) {
				// cares only about the first IP if there is a list
				ipAddress = ipAddress.replaceFirst(",.*", "");
			} else {
				ipAddress = request.getRemoteAddr();
			}
			return ipAddress;
		}

	}

	@Getter
	@Setter
	public static class LablePrinterHandler {
		private String testPage;

		/**
		 * Transformer for labelprinters
		 */
		private List<LabelPrinter> printer;

		/**
		 * Transformer for labelprinters
		 */
		private DefaultTransformer<LabelPrinter> printerTransformer;

		public LabelPrinter findPrinterByID(String id) {

			for (LabelPrinter labelPrinter : getPrinter()) {
				if (labelPrinter.getId() == Long.valueOf(id)) {
					return labelPrinter;
				}
			}
			return null;
		}
	}

}

// Type listType=new
// TypeToken<ArrayList<LabelPrinter>>(){}.getType();setLabelPrinterList(gson.fromJson(o.get(SETTINGS_LABLE_PRINTERS),listType));setLabelPrinterListTransformer(new
// DefaultTransformer<LabelPrinter>(getLabelPrinterList()));
//
// printerForRoomHandler=gson.fromJson(o.get(SETTINGS_PRINTER_FOR_ROOM),PrinterForRoomHandler.class);
//
// @Autowired
// @Getter(AccessLevel.NONE)
// @Setter(AccessLevel.NONE)
// private GlobalSettings globalSettings;
//
// public List<ClinicPrinter> loadCupsPrinters(PrinterSettings settings,
// DefaultDocuments defaultDocuments) {
//
//
// return result;
// }