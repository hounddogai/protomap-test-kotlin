package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLockerSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedLockerSegment("code-705", "name-130")
        assertEquals(WeightedLockerSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedLockerSegment("code-705", "name-130")
        try {
            item.moveTo(WeightedLockerSegmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLockerSegmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedLockerSegment("code-705", "name-130")
        item.status = WeightedLockerSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedLockerSegmentStatus.PLANNED) }
    }
}
