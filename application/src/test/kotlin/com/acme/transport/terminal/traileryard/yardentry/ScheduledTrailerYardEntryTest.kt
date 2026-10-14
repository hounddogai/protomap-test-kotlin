package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerYardEntryTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledTrailerYardEntry("code-728", "name-713")
        assertEquals(ScheduledTrailerYardEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledTrailerYardEntry("code-728", "name-713")
        try {
            item.moveTo(ScheduledTrailerYardEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerYardEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledTrailerYardEntry("code-728", "name-713")
        item.status = ScheduledTrailerYardEntryStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledTrailerYardEntryStatus.NEW) }
    }
}
