package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalViolationSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalViolationSegment("code-169", "title-248")
        assertEquals(GlobalViolationSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalViolationSegment("code-169", "title-248")
        try {
            item.moveTo(GlobalViolationSegmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalViolationSegmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalViolationSegment("code-169", "title-248")
        item.status = GlobalViolationSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalViolationSegmentStatus.PROPOSED) }
    }
}
