package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLeaseReturnReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictLeaseReturnReservation("code-87", "name-753")
        assertEquals(StrictLeaseReturnReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictLeaseReturnReservation("code-87", "name-753")
        try {
            item.moveTo(StrictLeaseReturnReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLeaseReturnReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictLeaseReturnReservation("code-87", "name-753")
        item.status = StrictLeaseReturnReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictLeaseReturnReservationStatus.PLANNED) }
    }
}
