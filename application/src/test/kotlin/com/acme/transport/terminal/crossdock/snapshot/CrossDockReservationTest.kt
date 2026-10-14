package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newCrossDockReservation("code-4", "title-581")
        assertEquals(CrossDockReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCrossDockReservation("code-4", "title-581")
        try {
            item.moveTo(CrossDockReservationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockReservationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCrossDockReservation("code-4", "title-581")
        item.status = CrossDockReservationStatus.CLOSED
        assertFails("move") { item.moveTo(CrossDockReservationStatus.DRAFT) }
    }
}
