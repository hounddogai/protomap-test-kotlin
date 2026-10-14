package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationRollup("code-71", "title-204")
        assertEquals(SortationRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSortationRollup("code-71", "title-204")
        try {
            item.moveTo(SortationRollupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationRollupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationRollup("code-71", "title-204")
        item.status = SortationRollupStatus.CLOSED
        assertFails("move") { item.moveTo(SortationRollupStatus.DRAFT) }
    }
}
