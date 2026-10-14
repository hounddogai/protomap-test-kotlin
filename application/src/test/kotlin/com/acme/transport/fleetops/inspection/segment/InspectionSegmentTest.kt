package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionSegment("code-358", "label-852")
        assertEquals(InspectionSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newInspectionSegment("code-358", "label-852")
        try {
            item.moveTo(InspectionSegmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionSegmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionSegment("code-358", "label-852")
        item.status = InspectionSegmentStatus.FAILED
        assertFails("move") { item.moveTo(InspectionSegmentStatus.NEW) }
    }
}
