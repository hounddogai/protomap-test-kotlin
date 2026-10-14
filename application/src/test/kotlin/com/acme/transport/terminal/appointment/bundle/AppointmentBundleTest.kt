package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentBundleTest {
    @Test
    fun `starts in new`() {
        val item = newAppointmentBundle("code-634", "label-365")
        assertEquals(AppointmentBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAppointmentBundle("code-634", "label-365")
        try {
            item.moveTo(AppointmentBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAppointmentBundle("code-634", "label-365")
        item.status = AppointmentBundleStatus.FAILED
        assertFails("move") { item.moveTo(AppointmentBundleStatus.NEW) }
    }
}
