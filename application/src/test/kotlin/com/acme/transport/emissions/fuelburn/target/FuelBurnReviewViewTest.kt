package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnReview("code-528", "name-622")
        val view = toFuelBurnReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnReviewViews(mutableListOf<FuelBurnReview>(item, item)).size, "views")
    }
}
