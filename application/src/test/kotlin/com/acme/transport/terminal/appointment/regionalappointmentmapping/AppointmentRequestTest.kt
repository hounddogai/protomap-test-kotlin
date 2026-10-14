package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentRequestTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentRequest("code-947", "title-853")
        assertEquals(AppointmentRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAppointmentRequest("code-947", "title-853")
        try {
            item.moveTo(AppointmentRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentRequest("code-947", "title-853")
        item.status = AppointmentRequestStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentRequestStatus.CREATED) }
    }
}
