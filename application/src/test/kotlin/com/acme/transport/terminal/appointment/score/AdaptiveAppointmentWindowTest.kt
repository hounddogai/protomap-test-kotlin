package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAppointmentWindowTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveAppointmentWindow("code-148", "title-385")
        assertEquals(AdaptiveAppointmentWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newAdaptiveAppointmentWindow("code-148", "title-385")
        try {
            item.moveTo(AdaptiveAppointmentWindowStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAppointmentWindowStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveAppointmentWindow("code-148", "title-385")
        item.status = AdaptiveAppointmentWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveAppointmentWindowStatus.OPEN) }
    }
}
