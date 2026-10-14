package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeReservationTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeReservation("code-472", "label-137")
        assertEquals(EvChargeReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newEvChargeReservation("code-472", "label-137")
        try {
            item.moveTo(EvChargeReservationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeReservationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeReservation("code-472", "label-137")
        item.status = EvChargeReservationStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeReservationStatus.NEW) }
    }
}
