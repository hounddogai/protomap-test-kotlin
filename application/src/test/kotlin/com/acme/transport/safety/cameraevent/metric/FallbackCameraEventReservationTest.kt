package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCameraEventReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        assertEquals(FallbackCameraEventReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        try {
            item.moveTo(FallbackCameraEventReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCameraEventReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        item.status = FallbackCameraEventReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackCameraEventReservationStatus.REQUESTED) }
    }
}
