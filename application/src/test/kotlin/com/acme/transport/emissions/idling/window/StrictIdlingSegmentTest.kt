package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictIdlingSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictIdlingSegment("code-247", "title-726")
        assertEquals(StrictIdlingSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newStrictIdlingSegment("code-247", "title-726")
        try {
            item.moveTo(StrictIdlingSegmentStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictIdlingSegmentStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictIdlingSegment("code-247", "title-726")
        item.status = StrictIdlingSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(StrictIdlingSegmentStatus.IDLE) }
    }
}
