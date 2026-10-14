package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardLedger("code-816", "title-895")
        val view = toScorecardLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardLedgerViews(mutableListOf<ScorecardLedger>(item, item)).size, "views")
    }
}
