package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIdlingSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalIdlingSegment("code-684", "name-584")
        assertEquals(GlobalIdlingSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalIdlingSegment("code-684", "name-584")
        try {
            item.moveTo(GlobalIdlingSegmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIdlingSegmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalIdlingSegment("code-684", "name-584")
        item.status = GlobalIdlingSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalIdlingSegmentStatus.DRAFT) }
    }
}
