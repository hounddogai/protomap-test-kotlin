package com.acme.transport.terminal.sortation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSortationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalSortationDraft("code-191", "title-803")
        val view = toGlobalSortationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalSortationDraftViews(mutableListOf<GlobalSortationDraft>(item, item)).size, "views")
    }
}
