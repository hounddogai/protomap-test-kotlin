package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAppointmentBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalAppointmentBudget("code-440", "label-553")
        assertEquals(GlobalAppointmentBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalAppointmentBudget("code-440", "label-553")
        try {
            item.moveTo(GlobalAppointmentBudgetStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAppointmentBudgetStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalAppointmentBudget("code-440", "label-553")
        item.status = GlobalAppointmentBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalAppointmentBudgetStatus.PROPOSED) }
    }
}
