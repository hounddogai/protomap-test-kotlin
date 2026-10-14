package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyEmissionFactorPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyEmissionFactorPreference("code-97", "title-444")
        val view = toLegacyEmissionFactorPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyEmissionFactorPreferenceViews(mutableListOf<LegacyEmissionFactorPreference>(item, item)).size, "views")
    }
}
