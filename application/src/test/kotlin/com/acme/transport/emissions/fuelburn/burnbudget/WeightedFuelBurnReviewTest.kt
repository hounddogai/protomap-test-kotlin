package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnReviewTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedFuelBurnReview("code-32", "name-881")
        assertEquals(WeightedFuelBurnReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newWeightedFuelBurnReview("code-32", "name-881")
        try {
            item.moveTo(WeightedFuelBurnReviewStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnReviewStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedFuelBurnReview("code-32", "name-881")
        item.status = WeightedFuelBurnReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedFuelBurnReviewStatus.OPEN) }
    }
}
