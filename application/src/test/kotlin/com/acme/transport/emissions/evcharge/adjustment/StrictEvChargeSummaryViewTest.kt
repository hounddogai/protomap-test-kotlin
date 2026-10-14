package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEvChargeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEvChargeSummary("code-47", "label-781")
        val view = toStrictEvChargeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEvChargeSummaryViews(mutableListOf<StrictEvChargeSummary>(item, item)).size, "views")
    }
}
