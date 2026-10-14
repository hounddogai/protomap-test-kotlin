package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAvailabilityBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAvailabilityBaseline("code-416", "name-199")
        val view = toPrimaryAvailabilityBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAvailabilityBaselineViews(mutableListOf<PrimaryAvailabilityBaseline>(item, item)).size, "views")
    }
}
