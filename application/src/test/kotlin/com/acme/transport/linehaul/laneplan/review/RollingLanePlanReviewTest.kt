package com.acme.transport.linehaul.laneplan.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLanePlanReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingLanePlanReview("code-604", "title-146")
        assertEquals(RollingLanePlanReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingLanePlanReview("code-604", "title-146")
        try {
            item.moveTo(RollingLanePlanReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLanePlanReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingLanePlanReview("code-604", "title-146")
        item.status = RollingLanePlanReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingLanePlanReviewStatus.PROPOSED) }
    }
}
