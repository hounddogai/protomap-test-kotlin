package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedScorecardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedScorecardNotice("code-950", "label-835")
        val view = toCappedScorecardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedScorecardNoticeViews(mutableListOf<CappedScorecardNotice>(item, item)).size, "views")
    }
}
