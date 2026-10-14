package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newAppointmentOverride("code-45", "name-651")
        assertEquals(AppointmentOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAppointmentOverride("code-45", "name-651")
        try {
            item.moveTo(AppointmentOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAppointmentOverride("code-45", "name-651")
        item.status = AppointmentOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(AppointmentOverrideStatus.REQUESTED) }
    }
}
