package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldSegment("code-8", "title-9")
        assertEquals(HoldSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newHoldSegment("code-8", "title-9")
        try {
            item.moveTo(HoldSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldSegment("code-8", "title-9")
        item.status = HoldSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(HoldSegmentStatus.IDLE) }
    }
}
