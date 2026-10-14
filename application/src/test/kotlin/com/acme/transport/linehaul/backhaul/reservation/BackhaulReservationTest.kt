package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newBackhaulReservation("code-362", "label-428")
        assertEquals(BackhaulReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newBackhaulReservation("code-362", "label-428")
        try {
            item.moveTo(BackhaulReservationStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulReservationStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBackhaulReservation("code-362", "label-428")
        item.status = BackhaulReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(BackhaulReservationStatus.REQUESTED) }
    }
}
