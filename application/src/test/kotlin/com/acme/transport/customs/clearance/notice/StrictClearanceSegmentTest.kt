package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictClearanceSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictClearanceSegment("code-165", "title-524")
        assertEquals(StrictClearanceSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictClearanceSegment("code-165", "title-524")
        try {
            item.moveTo(StrictClearanceSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictClearanceSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictClearanceSegment("code-165", "title-524")
        item.status = StrictClearanceSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(StrictClearanceSegmentStatus.IDLE) }
    }
}
