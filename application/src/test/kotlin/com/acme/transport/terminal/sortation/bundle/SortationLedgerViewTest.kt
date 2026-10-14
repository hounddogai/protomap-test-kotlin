package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationLedger("code-575", "title-185")
        val view = toSortationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationLedgerViews(mutableListOf<SortationLedger>(item, item)).size, "views")
    }
}
