package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAppointmentCycleTest {
    @Test
    fun `starts in created`() {
        val item = newPendingAppointmentCycle("code-906", "name-311")
        assertEquals(PendingAppointmentCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingAppointmentCycle("code-906", "name-311")
        try {
            item.moveTo(PendingAppointmentCycleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAppointmentCycleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingAppointmentCycle("code-906", "name-311")
        item.status = PendingAppointmentCycleStatus.RETIRED
        assertFails("move") { item.moveTo(PendingAppointmentCycleStatus.CREATED) }
    }
}
