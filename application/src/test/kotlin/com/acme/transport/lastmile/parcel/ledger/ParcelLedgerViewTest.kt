package com.acme.transport.lastmile.parcel.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelLedger("code-444", "label-714")
        val view = toParcelLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelLedgerViews(mutableListOf<ParcelLedger>(item, item)).size, "views")
    }
}
