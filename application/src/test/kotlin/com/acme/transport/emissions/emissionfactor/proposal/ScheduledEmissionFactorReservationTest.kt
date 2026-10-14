package com.acme.transport.emissions.emissionfactor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledEmissionFactorReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledEmissionFactorReservation("code-334", "title-921")
        assertEquals(ScheduledEmissionFactorReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledEmissionFactorReservation("code-334", "title-921")
        try {
            item.moveTo(ScheduledEmissionFactorReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledEmissionFactorReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledEmissionFactorReservation("code-334", "title-921")
        item.status = ScheduledEmissionFactorReservationStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledEmissionFactorReservationStatus.IDLE) }
    }
}
