package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackAppointmentBaseline("code-290", "title-521")
        assertEquals(FallbackAppointmentBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackAppointmentBaseline("code-290", "title-521")
        try {
            item.moveTo(FallbackAppointmentBaselineStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentBaselineStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackAppointmentBaseline("code-290", "title-521")
        item.status = FallbackAppointmentBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackAppointmentBaselineStatus.IDLE) }
    }
}
