package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationSegment("code-717", "label-831")
        assertEquals(UtilizationSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newUtilizationSegment("code-717", "label-831")
        try {
            item.moveTo(UtilizationSegmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationSegmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationSegment("code-717", "label-831")
        item.status = UtilizationSegmentStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationSegmentStatus.NEW) }
    }
}
