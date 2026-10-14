package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorPreference("code-881", "label-440")
        val view = toEmissionFactorPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorPreferenceViews(mutableListOf<EmissionFactorPreference>(item, item)).size, "views")
    }
}
