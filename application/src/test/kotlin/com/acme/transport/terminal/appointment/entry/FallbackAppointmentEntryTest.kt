package com.acme.transport.terminal.appointment.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackAppointmentEntry("code-280", "title-264")
        assertEquals(FallbackAppointmentEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFallbackAppointmentEntry("code-280", "title-264")
        try {
            item.moveTo(FallbackAppointmentEntryStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentEntryStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackAppointmentEntry("code-280", "title-264")
        item.status = FallbackAppointmentEntryStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackAppointmentEntryStatus.DRAFT) }
    }
}
