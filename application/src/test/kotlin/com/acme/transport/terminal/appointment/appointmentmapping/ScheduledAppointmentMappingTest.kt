package com.acme.transport.terminal.appointment.appointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAppointmentMappingTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledAppointmentMapping("code-270", "title-999")
        assertEquals(ScheduledAppointmentMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScheduledAppointmentMapping("code-270", "title-999")
        try {
            item.moveTo(ScheduledAppointmentMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAppointmentMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledAppointmentMapping("code-270", "title-999")
        item.status = ScheduledAppointmentMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledAppointmentMappingStatus.OPEN) }
    }
}
