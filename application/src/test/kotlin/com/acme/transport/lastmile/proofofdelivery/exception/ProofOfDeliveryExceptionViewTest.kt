package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryException("code-224", "name-844")
        val view = toProofOfDeliveryExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryExceptionViews(mutableListOf<ProofOfDeliveryException>(item, item)).size, "views")
    }
}
