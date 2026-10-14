package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEvChargeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEvChargeMapping("code-85", "name-209")
        val view = toWeightedEvChargeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEvChargeMappingViews(mutableListOf<WeightedEvChargeMapping>(item, item)).size, "views")
    }
}
