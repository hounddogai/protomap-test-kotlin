package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorReservation("code-668", "title-881")
        val view = toEmissionFactorReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorReservationViews(mutableListOf<EmissionFactorReservation>(item, item)).size, "views")
    }
}
