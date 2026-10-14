package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedScorecardProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedScorecardProfile("code-234", "label-427")
        val view = toWeightedScorecardProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedScorecardProfileViews(mutableListOf<WeightedScorecardProfile>(item, item)).size, "views")
    }
}
