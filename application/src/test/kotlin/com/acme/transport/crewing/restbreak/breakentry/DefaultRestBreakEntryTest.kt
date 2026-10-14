package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRestBreakEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultRestBreakEntry("code-891", "name-666")
        assertEquals(DefaultRestBreakEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultRestBreakEntry("code-891", "name-666")
        try {
            item.moveTo(DefaultRestBreakEntryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRestBreakEntryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultRestBreakEntry("code-891", "name-666")
        item.status = DefaultRestBreakEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultRestBreakEntryStatus.PLANNED) }
    }
}
