package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        val view = toProofOfDeliveryAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryAgreementViews(mutableListOf<ProofOfDeliveryAgreement>(item, item)).size, "views")
    }
}
