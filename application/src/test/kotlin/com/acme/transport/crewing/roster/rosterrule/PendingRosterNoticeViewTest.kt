package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRosterNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRosterNotice("code-941", "name-555")
        val view = toPendingRosterNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRosterNoticeViews(mutableListOf<PendingRosterNotice>(item, item)).size, "views")
    }
}
