package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRestBreakEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalRestBreakEstimate("code-18", "name-799")
        assertEquals(SeasonalRestBreakEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalRestBreakEstimate("code-18", "name-799")
        try {
            item.moveTo(SeasonalRestBreakEstimateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRestBreakEstimateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalRestBreakEstimate("code-18", "name-799")
        item.status = SeasonalRestBreakEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalRestBreakEstimateStatus.PLANNED) }
    }
}
