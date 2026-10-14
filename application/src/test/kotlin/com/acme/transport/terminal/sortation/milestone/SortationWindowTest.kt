package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationWindow("code-189", "title-890")
        assertEquals(SortationWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSortationWindow("code-189", "title-890")
        try {
            item.moveTo(SortationWindowStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationWindowStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationWindow("code-189", "title-890")
        item.status = SortationWindowStatus.CLOSED
        assertFails("move") { item.moveTo(SortationWindowStatus.DRAFT) }
    }
}
