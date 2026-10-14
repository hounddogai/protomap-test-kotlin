package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEmissionFactorVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEmissionFactorVariance("code-124", "label-278")
        val view = toAdaptiveEmissionFactorVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEmissionFactorVarianceViews(mutableListOf<AdaptiveEmissionFactorVariance>(item, item)).size, "views")
    }
}
