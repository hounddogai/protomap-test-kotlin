package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newLanePlanSegment("code-351", "title-472")
        assertEquals(LanePlanSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLanePlanSegment("code-351", "title-472")
        try {
            item.moveTo(LanePlanSegmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanSegmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLanePlanSegment("code-351", "title-472")
        item.status = LanePlanSegmentStatus.FAILED
        assertFails("move") { item.moveTo(LanePlanSegmentStatus.NEW) }
    }
}
