package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnReviewTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnReview("code-528", "name-622")
        assertEquals(FuelBurnReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFuelBurnReview("code-528", "name-622")
        try {
            item.moveTo(FuelBurnReviewStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnReviewStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnReview("code-528", "name-622")
        item.status = FuelBurnReviewStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnReviewStatus.CREATED) }
    }
}
