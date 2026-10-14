package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDwellSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDwellSummary("code-696", "label-219")
        val view = toWeightedDwellSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDwellSummaryViews(mutableListOf<WeightedDwellSummary>(item, item)).size, "views")
    }
}
