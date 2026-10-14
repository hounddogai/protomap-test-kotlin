package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAppointmentSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyAppointmentSegment("code-799", "name-271")
        assertEquals(LegacyAppointmentSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLegacyAppointmentSegment("code-799", "name-271")
        try {
            item.moveTo(LegacyAppointmentSegmentStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAppointmentSegmentStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyAppointmentSegment("code-799", "name-271")
        item.status = LegacyAppointmentSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyAppointmentSegmentStatus.OPEN) }
    }
}
