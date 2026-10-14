package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerCycle("code-99", "title-364")
        assertEquals(TrailerCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTrailerCycle("code-99", "title-364")
        try {
            item.moveTo(TrailerCycleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerCycleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerCycle("code-99", "title-364")
        item.status = TrailerCycleStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerCycleStatus.IDLE) }
    }
}
