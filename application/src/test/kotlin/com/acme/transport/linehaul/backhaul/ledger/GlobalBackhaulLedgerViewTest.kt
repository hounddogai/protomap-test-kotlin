package com.acme.transport.linehaul.backhaul.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBackhaulLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        val view = toGlobalBackhaulLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBackhaulLedgerViews(mutableListOf<GlobalBackhaulLedger>(item, item)).size, "views")
    }
}
