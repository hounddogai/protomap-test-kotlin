package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDisputeLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDisputeLedger("code-722", "label-11")
        val view = toPrimaryDisputeLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDisputeLedgerViews(mutableListOf<PrimaryDisputeLedger>(item, item)).size, "views")
    }
}
