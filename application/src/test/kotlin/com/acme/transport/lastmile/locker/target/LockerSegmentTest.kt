package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newLockerSegment("code-309", "label-646")
        assertEquals(LockerSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLockerSegment("code-309", "label-646")
        try {
            item.moveTo(LockerSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLockerSegment("code-309", "label-646")
        item.status = LockerSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(LockerSegmentStatus.IDLE) }
    }
}
