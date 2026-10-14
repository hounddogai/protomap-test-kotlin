package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAppointmentBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingAppointmentBundle("code-622", "label-590")
        assertEquals(RollingAppointmentBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingAppointmentBundle("code-622", "label-590")
        try {
            item.moveTo(RollingAppointmentBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAppointmentBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingAppointmentBundle("code-622", "label-590")
        item.status = RollingAppointmentBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingAppointmentBundleStatus.REQUESTED) }
    }
}
