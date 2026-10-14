package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingScorecardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingScorecardNotice("code-70", "title-425")
        val view = toRollingScorecardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingScorecardNoticeViews(mutableListOf<RollingScorecardNotice>(item, item)).size, "views")
    }
}
