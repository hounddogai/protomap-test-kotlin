package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAvailabilityPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAvailabilityPreference("code-71", "title-266")
        val view = toCappedAvailabilityPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAvailabilityPreferenceViews(mutableListOf<CappedAvailabilityPreference>(item, item)).size, "views")
    }
}
