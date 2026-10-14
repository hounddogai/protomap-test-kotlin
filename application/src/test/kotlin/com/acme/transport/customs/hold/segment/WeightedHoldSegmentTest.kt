package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedHoldSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedHoldSegment("code-171", "title-315")
        assertEquals(WeightedHoldSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedHoldSegment("code-171", "title-315")
        try {
            item.moveTo(WeightedHoldSegmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedHoldSegmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedHoldSegment("code-171", "title-315")
        item.status = WeightedHoldSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedHoldSegmentStatus.REQUESTED) }
    }
}
