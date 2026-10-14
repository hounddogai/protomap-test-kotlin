package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newCameraEventReservation("code-104", "title-163")
        assertEquals(CameraEventReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCameraEventReservation("code-104", "title-163")
        try {
            item.moveTo(CameraEventReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCameraEventReservation("code-104", "title-163")
        item.status = CameraEventReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(CameraEventReservationStatus.REQUESTED) }
    }
}
