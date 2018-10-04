package com.patho.main.config.security;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginHandler {

	public static final String LoginFaildSessionAttribue = "loginFailedMessage";
	
	public void removeLoginFailedMessagedAttribute(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(LoginFaildSessionAttribue);
		}
	}
}