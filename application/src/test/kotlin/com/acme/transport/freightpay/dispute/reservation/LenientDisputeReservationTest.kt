package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputeReservationTest {
    @Test
    fun `starts in new`() {
        val item = newLenientDisputeReservation("code-569", "name-366")
        assertEquals(LenientDisputeReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientDisputeReservation("code-569", "name-366")
        try {
            item.moveTo(LenientDisputeReservationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputeReservationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientDisputeReservation("code-569", "name-366")
        item.status = LenientDisputeReservationStatus.FAILED
        assertFails("move") { item.moveTo(LenientDisputeReservationStatus.NEW) }
    }
}
