package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRosterNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRosterNotice("code-362", "label-912")
        val view = toPrimaryRosterNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRosterNoticeViews(mutableListOf<PrimaryRosterNotice>(item, item)).size, "views")
    }
}
