package com.patho.main.model.transitory;

import java.util.ArrayList;
import java.util.List;

import com.patho.main.common.ContactRole;
import com.patho.main.model.patient.notification.AssociatedContactNotification;
import com.patho.main.util.helper.StreamUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultNotificationSettings {

	private List<DefaultNotification> defaultNotifications;

	public List<AssociatedContactNotification.NotificationTyp> getDefaultNotificationForRole(ContactRole role) {
		if (defaultNotifications != null) {
			try {
				return defaultNotifications.stream().filter(p -> p.getRole().equals(role))
						.collect(StreamUtils.singletonCollector()).getNotificationTyps();
			} catch (IllegalStateException e) {
				// returning empty list
			}
		}

		return new ArrayList<AssociatedContactNotification.NotificationTyp>();
	}

	@Getter
	@Setter
	public class DefaultNotification {
		private ContactRole role;
		private List<AssociatedContactNotification.NotificationTyp> notificationTyps;
	}
}
