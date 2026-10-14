package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAvailabilityRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAvailabilityRollup("code-850", "name-274")
        val view = toCappedAvailabilityRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAvailabilityRollupViews(mutableListOf<CappedAvailabilityRollup>(item, item)).size, "views")
    }
}
