package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargeReview("code-247", "label-292")
        assertEquals(FuelSurchargeReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFuelSurchargeReview("code-247", "label-292")
        try {
            item.moveTo(FuelSurchargeReviewStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeReviewStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargeReview("code-247", "label-292")
        item.status = FuelSurchargeReviewStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargeReviewStatus.DRAFT) }
    }
}
