package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAppointmentDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalAppointmentDigest("code-492", "name-297")
        assertEquals(SeasonalAppointmentDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalAppointmentDigest("code-492", "name-297")
        try {
            item.moveTo(SeasonalAppointmentDigestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAppointmentDigestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalAppointmentDigest("code-492", "name-297")
        item.status = SeasonalAppointmentDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalAppointmentDigestStatus.PROPOSED) }
    }
}
