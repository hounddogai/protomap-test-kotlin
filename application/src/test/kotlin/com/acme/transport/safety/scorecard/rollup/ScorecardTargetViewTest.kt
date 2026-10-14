package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardTarget("code-339", "name-978")
        val view = toScorecardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardTargetViews(mutableListOf<ScorecardTarget>(item, item)).size, "views")
    }
}
