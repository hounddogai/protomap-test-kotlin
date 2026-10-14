package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryLedger("code-319", "title-871")
        val view = toProofOfDeliveryLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryLedgerViews(mutableListOf<ProofOfDeliveryLedger>(item, item)).size, "views")
    }
}
