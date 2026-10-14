package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardAdjustment("code-846", "title-496")
        val view = toScorecardAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardAdjustmentViews(mutableListOf<ScorecardAdjustment>(item, item)).size, "views")
    }
}
