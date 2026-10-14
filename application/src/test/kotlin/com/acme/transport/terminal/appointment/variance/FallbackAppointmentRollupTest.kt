package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentRollupTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackAppointmentRollup("code-615", "name-593")
        assertEquals(FallbackAppointmentRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackAppointmentRollup("code-615", "name-593")
        try {
            item.moveTo(FallbackAppointmentRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackAppointmentRollup("code-615", "name-593")
        item.status = FallbackAppointmentRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackAppointmentRollupStatus.OPEN) }
    }
}
