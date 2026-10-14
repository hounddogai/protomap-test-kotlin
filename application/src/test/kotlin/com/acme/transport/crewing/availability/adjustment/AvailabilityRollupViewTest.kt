package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityRollup("code-182", "label-516")
        val view = toAvailabilityRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityRollupViews(mutableListOf<AvailabilityRollup>(item, item)).size, "views")
    }
}
