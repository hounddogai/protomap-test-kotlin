package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationCycle("code-52", "title-568")
        assertEquals(SortationCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSortationCycle("code-52", "title-568")
        try {
            item.moveTo(SortationCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationCycle("code-52", "title-568")
        item.status = SortationCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationCycleStatus.REQUESTED) }
    }
}
