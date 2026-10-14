package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackClearanceClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackClearanceClaim("code-67", "label-934")
        val view = toFallbackClearanceClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackClearanceClaimViews(mutableListOf<FallbackClearanceClaim>(item, item)).size, "views")
    }
}
