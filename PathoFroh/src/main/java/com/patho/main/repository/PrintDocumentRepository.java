package com.patho.main.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;

public interface PrintDocumentRepository {

	public List<PrintDocument> findAllByTypes(DocumentType... types);

	public List<PrintDocument> findAllByTypes(List<DocumentType> types);

	public List<PrintDocument> findAll();

	public Optional<PrintDocument> findByID(long id);

	public Optional<PrintDocument> findByTypeAndDefault(DocumentType type);

	/**
	 * Loads documents with the correct document classes, copies content form
	 * document to the new object
	 * 
	 * @param document
	 * @return
	 */
	public static PrintDocument loadDocument(PrintDocument document) {
		PrintDocument copy;

		if (document.getTemplateName() == null)
			copy = (PrintDocument) document.clone();
		else {
			try {
				Class<?> myClass = Class.forName(document.getTemplateName());
				Constructor<?> constructor = myClass.getConstructor(new Class[] { PrintDocument.class });
				copy = (PrintDocument) constructor.newInstance(new Object[] { document });
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				copy = (PrintDocument) document.clone();
			}
		}
		return copy;
	}

	/**
	 * Loads the ui class for a template
	 * 
	 * @param document
	 * @return
	 */
	public static AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> loadUiClass(
			PrintDocument document) {
		return loadUiClass(document, null);
	}

	/**
	 * Loads the ui class for a template, if a sharedData object is passed the new
	 * object will share data with the an other one
	 * 
	 * @param document
	 * @param sharedData
	 * @return
	 */
	public static AbstractDocumentUi<? extends PrintDocument, ? extends AbstractDocumentUi.SharedData> loadUiClass(
			PrintDocument document, AbstractDocumentUi.SharedData sharedData) {

		if (document.getUiClass() == null) {
			return new AbstractDocumentUi<PrintDocument, AbstractDocumentUi.SharedData>(document,
					new AbstractDocumentUi.SharedData());
		} else {
			try {
				Class<?> myClass = Class.forName(document.getUiClass()).asSubclass(AbstractDocumentUi.class);

				if (sharedData == null) {
					Constructor<?> constructor = myClass.getConstructor(new Class[] { document.getClass() });
					return (AbstractDocumentUi<?, ?>) constructor.newInstance(new Object[] { document });
				} else {
					Constructor<?> constructor = myClass
							.getConstructor(new Class[] { document.getClass(), sharedData.getClass() });
					return (AbstractDocumentUi<?, ?>) constructor.newInstance(new Object[] { document, sharedData });
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return new AbstractDocumentUi<PrintDocument, AbstractDocumentUi.SharedData>(document,
				new AbstractDocumentUi.SharedData());
	}

	/**
	 * Returns the default document of a given type.
	 * 
	 * @param documents
	 * @param type
	 * @return
	 */
	public static Optional<PrintDocument> getByTypAndDefault(List<PrintDocument> documents, DocumentType type) {

		for (PrintDocument printDocument : documents) {
			if (printDocument.getDocumentType() == type && printDocument.isDefaultOfType())
				return Optional.ofNullable(printDocument);
		}
		return Optional.empty();
	}
}