package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalEmissionFactorExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        val view = toSeasonalEmissionFactorExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalEmissionFactorExceptionViews(mutableListOf<SeasonalEmissionFactorException>(item, item)).size, "views")
    }
}
