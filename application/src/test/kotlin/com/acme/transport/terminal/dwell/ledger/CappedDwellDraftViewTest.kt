package com.acme.transport.terminal.dwell.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDwellDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDwellDraft("code-734", "name-58")
        val view = toCappedDwellDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDwellDraftViews(mutableListOf<CappedDwellDraft>(item, item)).size, "views")
    }
}
