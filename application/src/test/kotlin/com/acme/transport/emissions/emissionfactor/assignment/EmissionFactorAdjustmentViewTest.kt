package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        val view = toEmissionFactorAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorAdjustmentViews(mutableListOf<EmissionFactorAdjustment>(item, item)).size, "views")
    }
}
