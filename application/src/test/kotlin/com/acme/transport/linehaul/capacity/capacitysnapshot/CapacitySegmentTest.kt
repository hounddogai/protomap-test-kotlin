package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacitySegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacitySegment("code-988", "label-919")
        assertEquals(CapacitySegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCapacitySegment("code-988", "label-919")
        try {
            item.moveTo(CapacitySegmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacitySegmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacitySegment("code-988", "label-919")
        item.status = CapacitySegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacitySegmentStatus.PROPOSED) }
    }
}
