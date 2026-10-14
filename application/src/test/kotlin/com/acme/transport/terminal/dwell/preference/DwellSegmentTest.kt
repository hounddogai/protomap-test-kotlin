package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDwellSegment("code-190", "label-62")
        assertEquals(DwellSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDwellSegment("code-190", "label-62")
        try {
            item.moveTo(DwellSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDwellSegment("code-190", "label-62")
        item.status = DwellSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(DwellSegmentStatus.IDLE) }
    }
}
