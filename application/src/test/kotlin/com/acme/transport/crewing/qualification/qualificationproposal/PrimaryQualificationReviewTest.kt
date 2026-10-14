package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryQualificationReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryQualificationReview("code-503", "label-502")
        assertEquals(PrimaryQualificationReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryQualificationReview("code-503", "label-502")
        try {
            item.moveTo(PrimaryQualificationReviewStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryQualificationReviewStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryQualificationReview("code-503", "label-502")
        item.status = PrimaryQualificationReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryQualificationReviewStatus.REQUESTED) }
    }
}
