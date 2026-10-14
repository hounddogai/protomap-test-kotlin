package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentTierTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentTier("code-958", "label-3")
        assertEquals(AppointmentTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAppointmentTier("code-958", "label-3")
        try {
            item.moveTo(AppointmentTierStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentTierStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentTier("code-958", "label-3")
        item.status = AppointmentTierStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentTierStatus.CREATED) }
    }
}
