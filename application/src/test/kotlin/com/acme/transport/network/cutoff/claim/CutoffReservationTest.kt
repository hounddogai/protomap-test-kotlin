package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newCutoffReservation("code-694", "label-370")
        assertEquals(CutoffReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCutoffReservation("code-694", "label-370")
        try {
            item.moveTo(CutoffReservationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffReservationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCutoffReservation("code-694", "label-370")
        item.status = CutoffReservationStatus.CLOSED
        assertFails("move") { item.moveTo(CutoffReservationStatus.DRAFT) }
    }
}
