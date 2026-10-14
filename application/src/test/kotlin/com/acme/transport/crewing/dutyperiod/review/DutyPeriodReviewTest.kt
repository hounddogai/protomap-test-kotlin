package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodReview("code-289", "name-567")
        assertEquals(DutyPeriodReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDutyPeriodReview("code-289", "name-567")
        try {
            item.moveTo(DutyPeriodReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodReview("code-289", "name-567")
        item.status = DutyPeriodReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodReviewStatus.PROPOSED) }
    }
}
