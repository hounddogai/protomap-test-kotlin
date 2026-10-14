package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAppointmentTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        assertEquals(CappedAppointmentTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        try {
            item.moveTo(CappedAppointmentTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAppointmentTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        item.status = CappedAppointmentTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedAppointmentTemplateStatus.REQUESTED) }
    }
}
