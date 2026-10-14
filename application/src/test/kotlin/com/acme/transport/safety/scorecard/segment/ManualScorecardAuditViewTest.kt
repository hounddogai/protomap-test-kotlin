package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualScorecardAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualScorecardAudit("code-547", "name-476")
        val view = toManualScorecardAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualScorecardAuditViews(mutableListOf<ManualScorecardAudit>(item, item)).size, "views")
    }
}
