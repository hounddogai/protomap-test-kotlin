package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAvailabilityScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAvailabilityScore("code-496", "title-717")
        val view = toPendingAvailabilityScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAvailabilityScoreViews(mutableListOf<PendingAvailabilityScore>(item, item)).size, "views")
    }
}
