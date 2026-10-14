package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentProposalTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentProposal("code-618", "label-612")
        assertEquals(AppointmentProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAppointmentProposal("code-618", "label-612")
        try {
            item.moveTo(AppointmentProposalStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentProposalStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentProposal("code-618", "label-612")
        item.status = AppointmentProposalStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentProposalStatus.CREATED) }
    }
}
