package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSortationBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledSortationBaseline("code-763", "name-824")
        assertEquals(ScheduledSortationBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledSortationBaseline("code-763", "name-824")
        try {
            item.moveTo(ScheduledSortationBaselineStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSortationBaselineStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledSortationBaseline("code-763", "name-824")
        item.status = ScheduledSortationBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledSortationBaselineStatus.DRAFT) }
    }
}
