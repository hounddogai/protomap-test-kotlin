package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLockerSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedLockerSegment("code-395", "label-848")
        assertEquals(CappedLockerSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedLockerSegment("code-395", "label-848")
        try {
            item.moveTo(CappedLockerSegmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLockerSegmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedLockerSegment("code-395", "label-848")
        item.status = CappedLockerSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(CappedLockerSegmentStatus.DRAFT) }
    }
}
