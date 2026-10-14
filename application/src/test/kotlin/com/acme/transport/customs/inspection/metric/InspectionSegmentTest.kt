package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionSegment("code-442", "label-410")
        assertEquals(InspectionSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newInspectionSegment("code-442", "label-410")
        try {
            item.moveTo(InspectionSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionSegment("code-442", "label-410")
        item.status = InspectionSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionSegmentStatus.IDLE) }
    }
}
