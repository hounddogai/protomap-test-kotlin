package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEmissionFactorReservationTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        assertEquals(FallbackEmissionFactorReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        try {
            item.moveTo(FallbackEmissionFactorReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEmissionFactorReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        item.status = FallbackEmissionFactorReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackEmissionFactorReservationStatus.OPEN) }
    }
}
