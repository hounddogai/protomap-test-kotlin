package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServiceAreaReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalServiceAreaReservation("code-436", "title-528")
        assertEquals(RegionalServiceAreaReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRegionalServiceAreaReservation("code-436", "title-528")
        try {
            item.moveTo(RegionalServiceAreaReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServiceAreaReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalServiceAreaReservation("code-436", "title-528")
        item.status = RegionalServiceAreaReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalServiceAreaReservationStatus.PLANNED) }
    }
}
