package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAppointmentBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        assertEquals(BatchedAppointmentBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        try {
            item.moveTo(BatchedAppointmentBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAppointmentBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        item.status = BatchedAppointmentBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedAppointmentBudgetStatus.IDLE) }
    }
}
