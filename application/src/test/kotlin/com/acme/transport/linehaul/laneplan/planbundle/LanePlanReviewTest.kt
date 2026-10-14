package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newLanePlanReview("code-77", "label-643")
        assertEquals(LanePlanReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLanePlanReview("code-77", "label-643")
        try {
            item.moveTo(LanePlanReviewStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanReviewStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLanePlanReview("code-77", "label-643")
        item.status = LanePlanReviewStatus.CLOSED
        assertFails("move") { item.moveTo(LanePlanReviewStatus.DRAFT) }
    }
}
