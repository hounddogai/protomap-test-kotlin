package com.acme.transport.lastmile.redelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRedeliveryLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        val view = toGlobalRedeliveryLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRedeliveryLedgerViews(mutableListOf<GlobalRedeliveryLedger>(item, item)).size, "views")
    }
}
