package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakSegment("code-316", "label-190")
        assertEquals(RestBreakSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRestBreakSegment("code-316", "label-190")
        try {
            item.moveTo(RestBreakSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakSegment("code-316", "label-190")
        item.status = RestBreakSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakSegmentStatus.IDLE) }
    }
}
