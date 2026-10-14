package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEmissionFactorTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEmissionFactorTemplate("code-867", "name-76")
        val view = toWeightedEmissionFactorTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEmissionFactorTemplateViews(mutableListOf<WeightedEmissionFactorTemplate>(item, item)).size, "views")
    }
}
