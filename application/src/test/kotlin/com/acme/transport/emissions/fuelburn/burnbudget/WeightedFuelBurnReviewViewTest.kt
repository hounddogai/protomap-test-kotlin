package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnReview("code-32", "name-881")
        val view = toWeightedFuelBurnReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnReviewViews(mutableListOf<WeightedFuelBurnReview>(item, item)).size, "views")
    }
}
