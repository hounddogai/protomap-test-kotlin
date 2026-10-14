package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeReservation("code-827", "title-541")
        assertEquals(DisputeReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDisputeReservation("code-827", "title-541")
        try {
            item.moveTo(DisputeReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeReservation("code-827", "title-541")
        item.status = DisputeReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeReservationStatus.REQUESTED) }
    }
}
