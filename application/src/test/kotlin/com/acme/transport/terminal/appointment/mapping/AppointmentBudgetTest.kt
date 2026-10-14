package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newAppointmentBudget("code-358", "title-880")
        assertEquals(AppointmentBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAppointmentBudget("code-358", "title-880")
        try {
            item.moveTo(AppointmentBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAppointmentBudget("code-358", "title-880")
        item.status = AppointmentBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(AppointmentBudgetStatus.OPEN) }
    }
}
