package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAppointmentAuditTest {
    @Test
    fun `starts in new`() {
        val item = newLenientAppointmentAudit("code-393", "label-366")
        assertEquals(LenientAppointmentAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLenientAppointmentAudit("code-393", "label-366")
        try {
            item.moveTo(LenientAppointmentAuditStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAppointmentAuditStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientAppointmentAudit("code-393", "label-366")
        item.status = LenientAppointmentAuditStatus.FAILED
        assertFails("move") { item.moveTo(LenientAppointmentAuditStatus.NEW) }
    }
}
