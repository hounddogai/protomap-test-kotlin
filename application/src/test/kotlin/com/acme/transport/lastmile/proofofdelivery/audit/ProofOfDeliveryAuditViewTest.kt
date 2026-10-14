package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryAudit("code-889", "label-247")
        val view = toProofOfDeliveryAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryAuditViews(mutableListOf<ProofOfDeliveryAudit>(item, item)).size, "views")
    }
}
