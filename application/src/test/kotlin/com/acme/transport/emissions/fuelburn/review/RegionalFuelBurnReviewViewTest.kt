package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFuelBurnReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFuelBurnReview("code-760", "title-218")
        val view = toRegionalFuelBurnReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFuelBurnReviewViews(mutableListOf<RegionalFuelBurnReview>(item, item)).size, "views")
    }
}
