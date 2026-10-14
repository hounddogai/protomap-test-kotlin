package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRestBreakScore("code-31", "title-459")
        assertEquals(WeightedRestBreakScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedRestBreakScore("code-31", "title-459")
        try {
            item.moveTo(WeightedRestBreakScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRestBreakScore("code-31", "title-459")
        item.status = WeightedRestBreakScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRestBreakScoreStatus.PLANNED) }
    }
}
