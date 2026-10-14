package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIncidentSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualIncidentSegment("code-499", "label-30")
        assertEquals(ManualIncidentSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualIncidentSegment("code-499", "label-30")
        try {
            item.moveTo(ManualIncidentSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIncidentSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualIncidentSegment("code-499", "label-30")
        item.status = ManualIncidentSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualIncidentSegmentStatus.PROPOSED) }
    }
}
