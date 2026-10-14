package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerGroupTest {
    @Test
    fun `starts in new`() {
        val item = newCappedTrailerGroup("code-661", "name-10")
        assertEquals(CappedTrailerGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedTrailerGroup("code-661", "name-10")
        try {
            item.moveTo(CappedTrailerGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedTrailerGroup("code-661", "name-10")
        item.status = CappedTrailerGroupStatus.FAILED
        assertFails("move") { item.moveTo(CappedTrailerGroupStatus.NEW) }
    }
}
