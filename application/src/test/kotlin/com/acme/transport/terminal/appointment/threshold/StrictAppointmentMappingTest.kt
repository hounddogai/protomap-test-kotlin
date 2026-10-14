package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAppointmentMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictAppointmentMapping("code-43", "title-963")
        assertEquals(StrictAppointmentMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictAppointmentMapping("code-43", "title-963")
        try {
            item.moveTo(StrictAppointmentMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAppointmentMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictAppointmentMapping("code-43", "title-963")
        item.status = StrictAppointmentMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictAppointmentMappingStatus.PLANNED) }
    }
}
