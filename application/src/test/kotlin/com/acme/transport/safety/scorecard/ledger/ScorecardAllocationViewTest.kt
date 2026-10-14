package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardAllocation("code-775", "title-557")
        val view = toScorecardAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardAllocationViews(mutableListOf<ScorecardAllocation>(item, item)).size, "views")
    }
}
