package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAppointmentCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveAppointmentCycle("code-235", "label-454")
        assertEquals(AdaptiveAppointmentCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveAppointmentCycle("code-235", "label-454")
        try {
            item.moveTo(AdaptiveAppointmentCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAppointmentCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveAppointmentCycle("code-235", "label-454")
        item.status = AdaptiveAppointmentCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveAppointmentCycleStatus.PLANNED) }
    }
}
