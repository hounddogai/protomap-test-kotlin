package com.acme.transport.terminal.appointment.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentException("code-143", "title-522")
        assertEquals(AppointmentExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAppointmentException("code-143", "title-522")
        try {
            item.moveTo(AppointmentExceptionStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentExceptionStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentException("code-143", "title-522")
        item.status = AppointmentExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentExceptionStatus.CREATED) }
    }
}
