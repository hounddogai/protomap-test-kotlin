package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictProofOfDeliveryReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictProofOfDeliveryReservation("code-894", "label-346")
        assertEquals(StrictProofOfDeliveryReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictProofOfDeliveryReservation("code-894", "label-346")
        try {
            item.moveTo(StrictProofOfDeliveryReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictProofOfDeliveryReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictProofOfDeliveryReservation("code-894", "label-346")
        item.status = StrictProofOfDeliveryReservationStatus.CLOSED
        assertFails("move") { item.moveTo(StrictProofOfDeliveryReservationStatus.DRAFT) }
    }
}
