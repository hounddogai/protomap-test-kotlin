package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        assertEquals(FallbackAppointmentGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        try {
            item.moveTo(FallbackAppointmentGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        item.status = FallbackAppointmentGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackAppointmentGroupStatus.PLANNED) }
    }
}
