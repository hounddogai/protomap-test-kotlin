package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredScorecardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredScorecardNotice("code-236", "title-931")
        val view = toTieredScorecardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredScorecardNoticeViews(mutableListOf<TieredScorecardNotice>(item, item)).size, "views")
    }
}
