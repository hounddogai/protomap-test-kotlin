package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAvailabilityReservationTest {
    @Test
    fun `starts in created`() {
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        assertEquals(CappedAvailabilityReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        try {
            item.moveTo(CappedAvailabilityReservationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAvailabilityReservationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        item.status = CappedAvailabilityReservationStatus.RETIRED
        assertFails("move") { item.moveTo(CappedAvailabilityReservationStatus.CREATED) }
    }
}
