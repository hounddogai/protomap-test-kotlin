package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentDraftTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentDraft("code-10", "name-106")
        assertEquals(AppointmentDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAppointmentDraft("code-10", "name-106")
        try {
            item.moveTo(AppointmentDraftStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentDraftStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentDraft("code-10", "name-106")
        item.status = AppointmentDraftStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentDraftStatus.CREATED) }
    }
}
