package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIdlingReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalIdlingReservation("code-664", "label-998")
        assertEquals(RegionalIdlingReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalIdlingReservation("code-664", "label-998")
        try {
            item.moveTo(RegionalIdlingReservationStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIdlingReservationStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalIdlingReservation("code-664", "label-998")
        item.status = RegionalIdlingReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalIdlingReservationStatus.REQUESTED) }
    }
}
