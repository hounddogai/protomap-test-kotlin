package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedProofOfDeliveryMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedProofOfDeliveryMapping("code-697", "name-231")
        val view = toBatchedProofOfDeliveryMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedProofOfDeliveryMappingViews(mutableListOf<BatchedProofOfDeliveryMapping>(item, item)).size, "views")
    }
}
