package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newSwapReservation("code-763", "label-637")
        assertEquals(SwapReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newSwapReservation("code-763", "label-637")
        try {
            item.moveTo(SwapReservationStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapReservationStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSwapReservation("code-763", "label-637")
        item.status = SwapReservationStatus.STOPPED
        assertFails("move") { item.moveTo(SwapReservationStatus.IDLE) }
    }
}
