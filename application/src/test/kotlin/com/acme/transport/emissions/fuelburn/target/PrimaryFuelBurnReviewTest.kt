package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelBurnReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryFuelBurnReview("code-533", "title-118")
        assertEquals(PrimaryFuelBurnReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPrimaryFuelBurnReview("code-533", "title-118")
        try {
            item.moveTo(PrimaryFuelBurnReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelBurnReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryFuelBurnReview("code-533", "title-118")
        item.status = PrimaryFuelBurnReviewStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryFuelBurnReviewStatus.IDLE) }
    }
}
