package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultProofOfDeliveryScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultProofOfDeliveryScore("code-157", "title-545")
        val view = toDefaultProofOfDeliveryScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultProofOfDeliveryScoreViews(mutableListOf<DefaultProofOfDeliveryScore>(item, item)).size, "views")
    }
}
