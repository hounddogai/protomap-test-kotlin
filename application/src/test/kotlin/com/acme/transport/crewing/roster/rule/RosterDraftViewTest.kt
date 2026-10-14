package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterDraft("code-993", "label-874")
        val view = toRosterDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterDraftViews(mutableListOf<RosterDraft>(item, item)).size, "views")
    }
}
