package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredNodeLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredNodeLedger("code-70", "label-126")
        val view = toTieredNodeLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredNodeLedgerViews(mutableListOf<TieredNodeLedger>(item, item)).size, "views")
    }
}
