package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newAccessorialReservation("code-197", "title-584")
        assertEquals(AccessorialReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAccessorialReservation("code-197", "title-584")
        try {
            item.moveTo(AccessorialReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAccessorialReservation("code-197", "title-584")
        item.status = AccessorialReservationStatus.STOPPED
        assertFails("move") { item.moveTo(AccessorialReservationStatus.IDLE) }
    }
}
