package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCutoffReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedCutoffReservation("code-949", "name-694")
        assertEquals(BatchedCutoffReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedCutoffReservation("code-949", "name-694")
        try {
            item.moveTo(BatchedCutoffReservationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCutoffReservationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedCutoffReservation("code-949", "name-694")
        item.status = BatchedCutoffReservationStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedCutoffReservationStatus.DRAFT) }
    }
}
