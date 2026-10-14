package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceClaim("code-867", "label-978")
        val view = toClearanceClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceClaimViews(mutableListOf<ClearanceClaim>(item, item)).size, "views")
    }
}
