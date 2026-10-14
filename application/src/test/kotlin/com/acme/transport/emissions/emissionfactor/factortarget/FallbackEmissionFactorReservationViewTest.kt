package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEmissionFactorReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        val view = toFallbackEmissionFactorReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEmissionFactorReservationViews(mutableListOf<FallbackEmissionFactorReservation>(item, item)).size, "views")
    }
}
