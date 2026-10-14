package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDockDoorSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        assertEquals(DefaultDockDoorSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        try {
            item.moveTo(DefaultDockDoorSegmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDockDoorSegmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        item.status = DefaultDockDoorSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultDockDoorSegmentStatus.PROPOSED) }
    }
}
