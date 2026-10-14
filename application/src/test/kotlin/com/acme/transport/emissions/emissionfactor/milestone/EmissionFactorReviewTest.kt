package com.acme.transport.emissions.emissionfactor.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorReviewTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorReview("code-36", "name-885")
        assertEquals(EmissionFactorReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEmissionFactorReview("code-36", "name-885")
        try {
            item.moveTo(EmissionFactorReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorReview("code-36", "name-885")
        item.status = EmissionFactorReviewStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorReviewStatus.NEW) }
    }
}
