package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceSegment("code-974", "title-584")
        assertEquals(ClearanceSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newClearanceSegment("code-974", "title-584")
        try {
            item.moveTo(ClearanceSegmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceSegmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceSegment("code-974", "title-584")
        item.status = ClearanceSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceSegmentStatus.CREATED) }
    }
}
