package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultScorecardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultScorecardNotice("code-616", "title-169")
        val view = toDefaultScorecardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultScorecardNoticeViews(mutableListOf<DefaultScorecardNotice>(item, item)).size, "views")
    }
}
