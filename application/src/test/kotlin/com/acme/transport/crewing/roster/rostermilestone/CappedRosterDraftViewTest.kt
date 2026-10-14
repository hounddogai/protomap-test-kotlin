package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRosterDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRosterDraft("code-7", "label-171")
        val view = toCappedRosterDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRosterDraftViews(mutableListOf<CappedRosterDraft>(item, item)).size, "views")
    }
}
