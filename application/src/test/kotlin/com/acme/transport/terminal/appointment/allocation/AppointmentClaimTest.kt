package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentClaimTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentClaim("code-648", "title-210")
        assertEquals(AppointmentClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAppointmentClaim("code-648", "title-210")
        try {
            item.moveTo(AppointmentClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentClaim("code-648", "title-210")
        item.status = AppointmentClaimStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentClaimStatus.CREATED) }
    }
}
