package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAppointmentBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveAppointmentBudget("code-882", "title-409")
        assertEquals(AdaptiveAppointmentBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveAppointmentBudget("code-882", "title-409")
        try {
            item.moveTo(AdaptiveAppointmentBudgetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAppointmentBudgetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveAppointmentBudget("code-882", "title-409")
        item.status = AdaptiveAppointmentBudgetStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveAppointmentBudgetStatus.NEW) }
    }
}
