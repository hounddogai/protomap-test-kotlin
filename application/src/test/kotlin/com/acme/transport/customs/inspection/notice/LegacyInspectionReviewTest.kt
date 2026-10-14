package com.acme.transport.customs.inspection.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionReviewTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyInspectionReview("code-216", "title-126")
        assertEquals(LegacyInspectionReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLegacyInspectionReview("code-216", "title-126")
        try {
            item.moveTo(LegacyInspectionReviewStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionReviewStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyInspectionReview("code-216", "title-126")
        item.status = LegacyInspectionReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyInspectionReviewStatus.OPEN) }
    }
}
