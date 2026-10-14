package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardAudit("code-355", "title-867")
        val view = toScorecardAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardAuditViews(mutableListOf<ScorecardAudit>(item, item)).size, "views")
    }
}
