package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorThreshold("code-218", "label-298")
        val view = toEmissionFactorThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorThresholdViews(mutableListOf<EmissionFactorThreshold>(item, item)).size, "views")
    }
}
