package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEmissionFactorReviewTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryEmissionFactorReview("code-102", "label-735")
        assertEquals(PrimaryEmissionFactorReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryEmissionFactorReview("code-102", "label-735")
        try {
            item.moveTo(PrimaryEmissionFactorReviewStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEmissionFactorReviewStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryEmissionFactorReview("code-102", "label-735")
        item.status = PrimaryEmissionFactorReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryEmissionFactorReviewStatus.OPEN) }
    }
}
