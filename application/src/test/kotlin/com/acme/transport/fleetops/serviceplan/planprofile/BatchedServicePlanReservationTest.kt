package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServicePlanReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedServicePlanReservation("code-216", "title-356")
        assertEquals(BatchedServicePlanReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBatchedServicePlanReservation("code-216", "title-356")
        try {
            item.moveTo(BatchedServicePlanReservationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServicePlanReservationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedServicePlanReservation("code-216", "title-356")
        item.status = BatchedServicePlanReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedServicePlanReservationStatus.PLANNED) }
    }
}
