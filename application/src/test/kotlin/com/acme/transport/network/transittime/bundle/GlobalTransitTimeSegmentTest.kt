package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTransitTimeSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalTransitTimeSegment("code-607", "label-228")
        assertEquals(GlobalTransitTimeSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newGlobalTransitTimeSegment("code-607", "label-228")
        try {
            item.moveTo(GlobalTransitTimeSegmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTransitTimeSegmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalTransitTimeSegment("code-607", "label-228")
        item.status = GlobalTransitTimeSegmentStatus.FAILED
        assertFails("move") { item.moveTo(GlobalTransitTimeSegmentStatus.NEW) }
    }
}
