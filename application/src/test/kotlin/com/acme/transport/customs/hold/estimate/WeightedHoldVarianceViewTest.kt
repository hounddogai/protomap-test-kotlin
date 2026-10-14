package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedHoldVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedHoldVariance("code-919", "label-843")
        val view = toWeightedHoldVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedHoldVarianceViews(mutableListOf<WeightedHoldVariance>(item, item)).size, "views")
    }
}
