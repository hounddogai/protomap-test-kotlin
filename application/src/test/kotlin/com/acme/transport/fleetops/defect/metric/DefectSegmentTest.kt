package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectSegment("code-243", "label-348")
        assertEquals(DefectSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefectSegment("code-243", "label-348")
        try {
            item.moveTo(DefectSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectSegment("code-243", "label-348")
        item.status = DefectSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectSegmentStatus.PROPOSED) }
    }
}
