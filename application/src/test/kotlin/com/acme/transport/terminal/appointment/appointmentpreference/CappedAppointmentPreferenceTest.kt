package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAppointmentPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedAppointmentPreference("code-327", "label-217")
        assertEquals(CappedAppointmentPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCappedAppointmentPreference("code-327", "label-217")
        try {
            item.moveTo(CappedAppointmentPreferenceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAppointmentPreferenceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedAppointmentPreference("code-327", "label-217")
        item.status = CappedAppointmentPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedAppointmentPreferenceStatus.PROPOSED) }
    }
}
