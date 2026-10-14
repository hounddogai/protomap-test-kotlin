package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEvChargeSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedEvChargeSegment("code-396", "label-47")
        assertEquals(WeightedEvChargeSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newWeightedEvChargeSegment("code-396", "label-47")
        try {
            item.moveTo(WeightedEvChargeSegmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEvChargeSegmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedEvChargeSegment("code-396", "label-47")
        item.status = WeightedEvChargeSegmentStatus.FAILED
        assertFails("move") { item.moveTo(WeightedEvChargeSegmentStatus.NEW) }
    }
}
