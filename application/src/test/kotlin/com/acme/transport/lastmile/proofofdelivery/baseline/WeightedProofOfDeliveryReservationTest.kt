package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedProofOfDeliveryReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedProofOfDeliveryReservation("code-429", "label-185")
        assertEquals(WeightedProofOfDeliveryReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedProofOfDeliveryReservation("code-429", "label-185")
        try {
            item.moveTo(WeightedProofOfDeliveryReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedProofOfDeliveryReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedProofOfDeliveryReservation("code-429", "label-185")
        item.status = WeightedProofOfDeliveryReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedProofOfDeliveryReservationStatus.REQUESTED) }
    }
}
