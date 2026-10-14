package com.acme.transport.safety.scorecard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackScorecardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackScorecardTarget("code-875", "name-251")
        val view = toFallbackScorecardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackScorecardTargetViews(mutableListOf<FallbackScorecardTarget>(item, item)).size, "views")
    }
}
