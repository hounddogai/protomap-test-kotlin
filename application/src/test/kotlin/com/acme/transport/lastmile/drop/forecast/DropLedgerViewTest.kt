package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DropLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropLedger("code-30", "name-982")
        val view = toDropLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropLedgerViews(mutableListOf<DropLedger>(item, item)).size, "views")
    }
}
