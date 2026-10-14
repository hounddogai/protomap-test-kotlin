package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEvChargeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEvChargeBaseline("code-847", "title-380")
        val view = toStrictEvChargeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEvChargeBaselineViews(mutableListOf<StrictEvChargeBaseline>(item, item)).size, "views")
    }
}
