package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAppointmentScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedAppointmentScore("code-559", "title-107")
        assertEquals(BatchedAppointmentScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedAppointmentScore("code-559", "title-107")
        try {
            item.moveTo(BatchedAppointmentScoreStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAppointmentScoreStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedAppointmentScore("code-559", "title-107")
        item.status = BatchedAppointmentScoreStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedAppointmentScoreStatus.DRAFT) }
    }
}
