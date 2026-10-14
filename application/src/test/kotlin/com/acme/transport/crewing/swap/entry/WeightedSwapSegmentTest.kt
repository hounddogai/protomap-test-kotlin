package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSwapSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedSwapSegment("code-976", "name-175")
        assertEquals(WeightedSwapSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedSwapSegment("code-976", "name-175")
        try {
            item.moveTo(WeightedSwapSegmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSwapSegmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedSwapSegment("code-976", "name-175")
        item.status = WeightedSwapSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedSwapSegmentStatus.PLANNED) }
    }
}
