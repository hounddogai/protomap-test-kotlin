package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorReservationTest {
    @Test
    fun `starts in created`() {
        val item = newEmissionFactorReservation("code-668", "title-881")
        assertEquals(EmissionFactorReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newEmissionFactorReservation("code-668", "title-881")
        try {
            item.moveTo(EmissionFactorReservationStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorReservationStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEmissionFactorReservation("code-668", "title-881")
        item.status = EmissionFactorReservationStatus.RETIRED
        assertFails("move") { item.moveTo(EmissionFactorReservationStatus.CREATED) }
    }
}
