package com.acme.transport.crewing.dutyperiod.periodreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newManualDutyPeriodReview("code-272", "title-384")
        assertEquals(ManualDutyPeriodReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualDutyPeriodReview("code-272", "title-384")
        try {
            item.moveTo(ManualDutyPeriodReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualDutyPeriodReview("code-272", "title-384")
        item.status = ManualDutyPeriodReviewStatus.STOPPED
        assertFails("move") { item.moveTo(ManualDutyPeriodReviewStatus.IDLE) }
    }
}
