package com.acme.transport.crewing.qualification.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationReviewTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationReview("code-791", "name-541")
        assertEquals(QualificationReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newQualificationReview("code-791", "name-541")
        try {
            item.moveTo(QualificationReviewStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationReviewStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationReview("code-791", "name-541")
        item.status = QualificationReviewStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationReviewStatus.CREATED) }
    }
}
