package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedCapacityReservation("code-229", "title-807")
        assertEquals(BatchedCapacityReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedCapacityReservation("code-229", "title-807")
        try {
            item.moveTo(BatchedCapacityReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedCapacityReservation("code-229", "title-807")
        item.status = BatchedCapacityReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedCapacityReservationStatus.PLANNED) }
    }
}
