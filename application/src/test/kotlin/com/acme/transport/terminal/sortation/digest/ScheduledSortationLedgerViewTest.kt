package com.acme.transport.terminal.sortation.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledSortationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledSortationLedger("code-478", "label-421")
        val view = toScheduledSortationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledSortationLedgerViews(mutableListOf<ScheduledSortationLedger>(item, item)).size, "views")
    }
}
