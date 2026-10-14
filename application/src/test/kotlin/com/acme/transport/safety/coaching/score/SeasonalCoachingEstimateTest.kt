package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        assertEquals(SeasonalCoachingEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        try {
            item.moveTo(SeasonalCoachingEstimateStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingEstimateStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        item.status = SeasonalCoachingEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalCoachingEstimateStatus.IDLE) }
    }
}
