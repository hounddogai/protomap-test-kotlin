package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterNotice("code-219", "title-556")
        val view = toRosterNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterNoticeViews(mutableListOf<RosterNotice>(item, item)).size, "views")
    }
}
