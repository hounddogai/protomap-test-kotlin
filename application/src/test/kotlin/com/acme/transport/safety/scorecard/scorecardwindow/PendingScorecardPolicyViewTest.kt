package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingScorecardPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingScorecardPolicy("code-414", "title-175")
        val view = toPendingScorecardPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingScorecardPolicyViews(mutableListOf<PendingScorecardPolicy>(item, item)).size, "views")
    }
}
