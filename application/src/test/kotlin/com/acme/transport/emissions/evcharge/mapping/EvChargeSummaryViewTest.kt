package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeSummary("code-288", "title-497")
        val view = toEvChargeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeSummaryViews(mutableListOf<EvChargeSummary>(item, item)).size, "views")
    }
}
