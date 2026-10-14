package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterReservationTest {
    @Test
    fun `starts in open`() {
        val item = newRosterReservation("code-494", "title-878")
        assertEquals(RosterReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRosterReservation("code-494", "title-878")
        try {
            item.moveTo(RosterReservationStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterReservationStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRosterReservation("code-494", "title-878")
        item.status = RosterReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(RosterReservationStatus.OPEN) }
    }
}
