package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalSortationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalSortationLedger("code-128", "name-919")
        val view = toRegionalSortationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalSortationLedgerViews(mutableListOf<RegionalSortationLedger>(item, item)).size, "views")
    }
}
