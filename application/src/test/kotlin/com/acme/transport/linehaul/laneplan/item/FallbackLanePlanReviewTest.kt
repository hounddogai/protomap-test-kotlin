package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanReviewTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackLanePlanReview("code-948", "label-304")
        assertEquals(FallbackLanePlanReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackLanePlanReview("code-948", "label-304")
        try {
            item.moveTo(FallbackLanePlanReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackLanePlanReview("code-948", "label-304")
        item.status = FallbackLanePlanReviewStatus.FAILED
        assertFails("move") { item.moveTo(FallbackLanePlanReviewStatus.NEW) }
    }
}
