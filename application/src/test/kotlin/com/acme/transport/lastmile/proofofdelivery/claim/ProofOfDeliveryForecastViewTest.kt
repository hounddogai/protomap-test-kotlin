package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryForecast("code-704", "title-793")
        val view = toProofOfDeliveryForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryForecastViews(mutableListOf<ProofOfDeliveryForecast>(item, item)).size, "views")
    }
}
