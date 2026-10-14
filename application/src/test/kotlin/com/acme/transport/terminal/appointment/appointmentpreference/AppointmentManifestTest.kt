package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentManifestTest {
    @Test
    fun `starts in new`() {
        val item = newAppointmentManifest("code-558", "name-381")
        assertEquals(AppointmentManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAppointmentManifest("code-558", "name-381")
        try {
            item.moveTo(AppointmentManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAppointmentManifest("code-558", "name-381")
        item.status = AppointmentManifestStatus.FAILED
        assertFails("move") { item.moveTo(AppointmentManifestStatus.NEW) }
    }
}
