package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingHubLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingHubLedger("code-943", "label-352")
        val view = toPendingHubLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingHubLedgerViews(mutableListOf<PendingHubLedger>(item, item)).size, "views")
    }
}
