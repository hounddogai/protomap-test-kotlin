package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAttemptEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalAttemptEstimate("code-763", "title-405")
        assertEquals(SeasonalAttemptEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalAttemptEstimate("code-763", "title-405")
        try {
            item.moveTo(SeasonalAttemptEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAttemptEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalAttemptEstimate("code-763", "title-405")
        item.status = SeasonalAttemptEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalAttemptEstimateStatus.PROPOSED) }
    }
}
