package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        val view = toProofOfDeliveryGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryGroupViews(mutableListOf<ProofOfDeliveryGroup>(item, item)).size, "views")
    }
}
