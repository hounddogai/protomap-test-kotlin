package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDockDoorSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalDockDoorSegment("code-965", "title-257")
        assertEquals(SeasonalDockDoorSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalDockDoorSegment("code-965", "title-257")
        try {
            item.moveTo(SeasonalDockDoorSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDockDoorSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalDockDoorSegment("code-965", "title-257")
        item.status = SeasonalDockDoorSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalDockDoorSegmentStatus.IDLE) }
    }
}
