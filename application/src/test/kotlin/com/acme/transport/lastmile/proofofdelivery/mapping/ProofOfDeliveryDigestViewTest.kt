package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryDigest("code-957", "label-82")
        val view = toProofOfDeliveryDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryDigestViews(mutableListOf<ProofOfDeliveryDigest>(item, item)).size, "views")
    }
}
