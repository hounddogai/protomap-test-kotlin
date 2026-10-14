package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCutoffLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCutoffLedger("code-745", "label-749")
        val view = toManualCutoffLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCutoffLedgerViews(mutableListOf<ManualCutoffLedger>(item, item)).size, "views")
    }
}
