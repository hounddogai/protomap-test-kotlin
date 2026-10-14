package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedProofOfDeliveryReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedProofOfDeliveryReservation("code-429", "label-185")
        val view = toWeightedProofOfDeliveryReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedProofOfDeliveryReservationViews(mutableListOf<WeightedProofOfDeliveryReservation>(item, item)).size, "views")
    }
}
