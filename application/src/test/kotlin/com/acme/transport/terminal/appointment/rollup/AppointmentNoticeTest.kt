package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentNotice("code-370", "name-41")
        assertEquals(AppointmentNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAppointmentNotice("code-370", "name-41")
        try {
            item.moveTo(AppointmentNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentNotice("code-370", "name-41")
        item.status = AppointmentNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentNoticeStatus.IDLE) }
    }
}
