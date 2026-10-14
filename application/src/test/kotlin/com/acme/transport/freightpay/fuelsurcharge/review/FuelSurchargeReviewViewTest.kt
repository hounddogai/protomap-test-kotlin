package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeReview("code-247", "label-292")
        val view = toFuelSurchargeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeReviewViews(mutableListOf<FuelSurchargeReview>(item, item)).size, "views")
    }
}
