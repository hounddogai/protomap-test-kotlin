package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAppointmentVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingAppointmentVariance("code-812", "name-688")
        assertEquals(PendingAppointmentVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingAppointmentVariance("code-812", "name-688")
        try {
            item.moveTo(PendingAppointmentVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAppointmentVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingAppointmentVariance("code-812", "name-688")
        item.status = PendingAppointmentVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingAppointmentVarianceStatus.PLANNED) }
    }
}
