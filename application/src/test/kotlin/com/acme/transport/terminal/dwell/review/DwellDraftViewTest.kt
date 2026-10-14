package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellDraft("code-424", "label-406")
        val view = toDwellDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellDraftViews(mutableListOf<DwellDraft>(item, item)).size, "views")
    }
}
