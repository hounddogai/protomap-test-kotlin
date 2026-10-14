package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultScorecardAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        val view = toDefaultScorecardAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultScorecardAllocationViews(mutableListOf<DefaultScorecardAllocation>(item, item)).size, "views")
    }
}
