package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientProofOfDeliveryReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientProofOfDeliveryReservation("code-936", "title-372")
        assertEquals(LenientProofOfDeliveryReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientProofOfDeliveryReservation("code-936", "title-372")
        try {
            item.moveTo(LenientProofOfDeliveryReservationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientProofOfDeliveryReservationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientProofOfDeliveryReservation("code-936", "title-372")
        item.status = LenientProofOfDeliveryReservationStatus.CLOSED
        assertFails("move") { item.moveTo(LenientProofOfDeliveryReservationStatus.DRAFT) }
    }
}
