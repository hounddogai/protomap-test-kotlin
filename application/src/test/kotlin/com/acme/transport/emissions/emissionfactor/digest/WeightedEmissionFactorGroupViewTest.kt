package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEmissionFactorGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEmissionFactorGroup("code-218", "name-45")
        val view = toWeightedEmissionFactorGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEmissionFactorGroupViews(mutableListOf<WeightedEmissionFactorGroup>(item, item)).size, "views")
    }
}
