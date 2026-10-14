package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictRosterReservation("code-625", "name-303")
        assertEquals(StrictRosterReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictRosterReservation("code-625", "name-303")
        try {
            item.moveTo(StrictRosterReservationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterReservationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictRosterReservation("code-625", "name-303")
        item.status = StrictRosterReservationStatus.CLOSED
        assertFails("move") { item.moveTo(StrictRosterReservationStatus.DRAFT) }
    }
}
