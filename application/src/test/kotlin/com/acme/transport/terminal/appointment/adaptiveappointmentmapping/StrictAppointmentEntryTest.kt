package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAppointmentEntryTest {
    @Test
    fun `starts in created`() {
        val item = newStrictAppointmentEntry("code-160", "label-479")
        assertEquals(StrictAppointmentEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newStrictAppointmentEntry("code-160", "label-479")
        try {
            item.moveTo(StrictAppointmentEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAppointmentEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictAppointmentEntry("code-160", "label-479")
        item.status = StrictAppointmentEntryStatus.RETIRED
        assertFails("move") { item.moveTo(StrictAppointmentEntryStatus.CREATED) }
    }
}
