package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAppointmentClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredAppointmentClaim("code-332", "title-711")
        assertEquals(TieredAppointmentClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTieredAppointmentClaim("code-332", "title-711")
        try {
            item.moveTo(TieredAppointmentClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAppointmentClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredAppointmentClaim("code-332", "title-711")
        item.status = TieredAppointmentClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredAppointmentClaimStatus.PLANNED) }
    }
}
