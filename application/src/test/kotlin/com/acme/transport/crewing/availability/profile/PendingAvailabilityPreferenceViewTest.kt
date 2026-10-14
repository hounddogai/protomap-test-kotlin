package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAvailabilityPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAvailabilityPreference("code-718", "label-872")
        val view = toPendingAvailabilityPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAvailabilityPreferenceViews(mutableListOf<PendingAvailabilityPreference>(item, item)).size, "views")
    }
}
