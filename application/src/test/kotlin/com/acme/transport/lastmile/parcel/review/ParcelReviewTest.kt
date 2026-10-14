package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelReviewTest {
    @Test
    fun `starts in created`() {
        val item = newParcelReview("code-667", "title-102")
        assertEquals(ParcelReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newParcelReview("code-667", "title-102")
        try {
            item.moveTo(ParcelReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelReview("code-667", "title-102")
        item.status = ParcelReviewStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelReviewStatus.CREATED) }
    }
}
