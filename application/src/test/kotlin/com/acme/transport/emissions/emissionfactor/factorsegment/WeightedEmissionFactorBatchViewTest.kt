package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEmissionFactorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEmissionFactorBatch("code-935", "name-451")
        val view = toWeightedEmissionFactorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEmissionFactorBatchViews(mutableListOf<WeightedEmissionFactorBatch>(item, item)).size, "views")
    }
}
