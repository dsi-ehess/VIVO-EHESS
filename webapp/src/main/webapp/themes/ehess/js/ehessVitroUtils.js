/* $This file is distributed under the terms of the license in LICENSE$ */

$(document).ready(function() {

	// Handle close button
	jQuery('.ui-button', 'section#welcome-msg-container').click(function() {
		jQuery('section#welcome-msg-container').remove();
	});
});
