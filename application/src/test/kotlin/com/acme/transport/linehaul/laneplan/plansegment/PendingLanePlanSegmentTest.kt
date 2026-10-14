package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLanePlanSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingLanePlanSegment("code-717", "title-24")
        assertEquals(PendingLanePlanSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPendingLanePlanSegment("code-717", "title-24")
        try {
            item.moveTo(PendingLanePlanSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLanePlanSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingLanePlanSegment("code-717", "title-24")
        item.status = PendingLanePlanSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(PendingLanePlanSegmentStatus.IDLE) }
    }
}
