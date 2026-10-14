package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAppointmentAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        assertEquals(SeasonalAppointmentAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        try {
            item.moveTo(SeasonalAppointmentAuditStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAppointmentAuditStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        item.status = SeasonalAppointmentAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalAppointmentAuditStatus.REQUESTED) }
    }
}
