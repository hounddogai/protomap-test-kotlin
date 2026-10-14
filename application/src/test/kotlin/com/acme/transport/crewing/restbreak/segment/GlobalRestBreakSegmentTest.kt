package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRestBreakSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalRestBreakSegment("code-234", "name-182")
        assertEquals(GlobalRestBreakSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalRestBreakSegment("code-234", "name-182")
        try {
            item.moveTo(GlobalRestBreakSegmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRestBreakSegmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalRestBreakSegment("code-234", "name-182")
        item.status = GlobalRestBreakSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalRestBreakSegmentStatus.PROPOSED) }
    }
}
