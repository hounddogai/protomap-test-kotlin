package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalEmissionFactorPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalEmissionFactorPreference("code-275", "title-669")
        val view = toSeasonalEmissionFactorPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalEmissionFactorPreferenceViews(mutableListOf<SeasonalEmissionFactorPreference>(item, item)).size, "views")
    }
}
