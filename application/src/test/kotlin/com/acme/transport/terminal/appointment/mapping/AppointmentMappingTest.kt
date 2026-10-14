package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newAppointmentMapping("code-525", "title-647")
        assertEquals(AppointmentMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAppointmentMapping("code-525", "title-647")
        try {
            item.moveTo(AppointmentMappingStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentMappingStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAppointmentMapping("code-525", "title-647")
        item.status = AppointmentMappingStatus.CLOSED
        assertFails("move") { item.moveTo(AppointmentMappingStatus.DRAFT) }
    }
}
