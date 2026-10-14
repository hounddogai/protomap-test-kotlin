package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneReservationTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledZoneReservation("code-440", "label-478")
        assertEquals(ScheduledZoneReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledZoneReservation("code-440", "label-478")
        try {
            item.moveTo(ScheduledZoneReservationStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneReservationStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledZoneReservation("code-440", "label-478")
        item.status = ScheduledZoneReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledZoneReservationStatus.OPEN) }
    }
}
