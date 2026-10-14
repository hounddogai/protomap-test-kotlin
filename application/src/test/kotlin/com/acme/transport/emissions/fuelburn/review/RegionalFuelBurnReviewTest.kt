package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnReviewTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalFuelBurnReview("code-760", "title-218")
        assertEquals(RegionalFuelBurnReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalFuelBurnReview("code-760", "title-218")
        try {
            item.moveTo(RegionalFuelBurnReviewStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnReviewStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalFuelBurnReview("code-760", "title-218")
        item.status = RegionalFuelBurnReviewStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalFuelBurnReviewStatus.CREATED) }
    }
}
