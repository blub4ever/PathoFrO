package com.patho.main.model.interfaces;

import com.patho.main.common.Dialog;

import java.beans.Transient;

/**
 * Alle Objekte werden nicht gel�scht, sondern nurch archiviert.
 *
 * @author andi
 */
public interface ArchivAble {

    public boolean isArchived();

    /**
     * Setzt das Objekt und alles Kinder als "archived"
     *
     * @param archived
     */
    public void setArchived(boolean archived);

    /**
     * Gibt den Namen des Objektes zur�ck
     *
     * @return
     */
    @Transient
    public default String getTextIdentifier() {
        return "";
    }

    /**
     * Gibt den Dilaog zum Archivieren zur�ck
     *
     * @return
     */
    @Transient
    public default Dialog getArchiveDialog() {
        return null;
    }
}
