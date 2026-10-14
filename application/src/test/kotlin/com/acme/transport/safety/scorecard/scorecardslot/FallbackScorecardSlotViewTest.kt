package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackScorecardSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackScorecardSlot("code-579", "title-633")
        val view = toFallbackScorecardSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackScorecardSlotViews(mutableListOf<FallbackScorecardSlot>(item, item)).size, "views")
    }
}
