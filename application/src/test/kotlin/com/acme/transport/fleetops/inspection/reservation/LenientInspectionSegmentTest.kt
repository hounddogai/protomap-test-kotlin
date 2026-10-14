package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientInspectionSegment("code-499", "label-145")
        assertEquals(LenientInspectionSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientInspectionSegment("code-499", "label-145")
        try {
            item.moveTo(LenientInspectionSegmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionSegmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientInspectionSegment("code-499", "label-145")
        item.status = LenientInspectionSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientInspectionSegmentStatus.REQUESTED) }
    }
}
