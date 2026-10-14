package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAppointmentVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        assertEquals(RegionalAppointmentVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        try {
            item.moveTo(RegionalAppointmentVarianceStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAppointmentVarianceStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        item.status = RegionalAppointmentVarianceStatus.FAILED
        assertFails("move") { item.moveTo(RegionalAppointmentVarianceStatus.NEW) }
    }
}
