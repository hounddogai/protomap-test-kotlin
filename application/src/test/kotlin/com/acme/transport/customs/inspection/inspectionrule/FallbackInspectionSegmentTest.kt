package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackInspectionSegment("code-249", "title-964")
        assertEquals(FallbackInspectionSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackInspectionSegment("code-249", "title-964")
        try {
            item.moveTo(FallbackInspectionSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackInspectionSegment("code-249", "title-964")
        item.status = FallbackInspectionSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackInspectionSegmentStatus.PROPOSED) }
    }
}
