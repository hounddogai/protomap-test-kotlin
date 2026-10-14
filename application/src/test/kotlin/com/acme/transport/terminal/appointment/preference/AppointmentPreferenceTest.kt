package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentPreference("code-895", "label-392")
        assertEquals(AppointmentPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAppointmentPreference("code-895", "label-392")
        try {
            item.moveTo(AppointmentPreferenceStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentPreferenceStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentPreference("code-895", "label-392")
        item.status = AppointmentPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentPreferenceStatus.CREATED) }
    }
}
