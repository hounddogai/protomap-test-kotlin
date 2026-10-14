package com.acme.transport.fleetops.tire.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTireReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingTireReservation("code-726", "title-950")
        assertEquals(RollingTireReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingTireReservation("code-726", "title-950")
        try {
            item.moveTo(RollingTireReservationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTireReservationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingTireReservation("code-726", "title-950")
        item.status = RollingTireReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingTireReservationStatus.PROPOSED) }
    }
}
