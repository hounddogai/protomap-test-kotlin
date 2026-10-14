package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRosterSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRosterSummary("code-263", "label-149")
        val view = toWeightedRosterSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRosterSummaryViews(mutableListOf<WeightedRosterSummary>(item, item)).size, "views")
    }
}
