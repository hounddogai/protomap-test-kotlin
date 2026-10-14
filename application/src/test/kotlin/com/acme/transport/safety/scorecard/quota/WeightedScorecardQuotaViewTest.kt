package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedScorecardQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedScorecardQuota("code-5", "name-820")
        val view = toWeightedScorecardQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedScorecardQuotaViews(mutableListOf<WeightedScorecardQuota>(item, item)).size, "views")
    }
}
