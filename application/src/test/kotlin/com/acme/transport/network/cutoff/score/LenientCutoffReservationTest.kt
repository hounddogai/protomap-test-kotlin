package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCutoffReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientCutoffReservation("code-852", "title-762")
        assertEquals(LenientCutoffReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLenientCutoffReservation("code-852", "title-762")
        try {
            item.moveTo(LenientCutoffReservationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCutoffReservationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientCutoffReservation("code-852", "title-762")
        item.status = LenientCutoffReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientCutoffReservationStatus.PLANNED) }
    }
}
