package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalEmissionFactorCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalEmissionFactorCycle("code-229", "label-172")
        val view = toSeasonalEmissionFactorCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalEmissionFactorCycleViews(mutableListOf<SeasonalEmissionFactorCycle>(item, item)).size, "views")
    }
}
