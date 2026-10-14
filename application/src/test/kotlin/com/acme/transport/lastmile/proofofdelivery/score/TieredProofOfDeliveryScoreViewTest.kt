package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredProofOfDeliveryScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        val view = toTieredProofOfDeliveryScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredProofOfDeliveryScoreViews(mutableListOf<TieredProofOfDeliveryScore>(item, item)).size, "views")
    }
}
