package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardRecord("code-850", "name-971")
        val view = toScorecardRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardRecordViews(mutableListOf<ScorecardRecord>(item, item)).size, "views")
    }
}
