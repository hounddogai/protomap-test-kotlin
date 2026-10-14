package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newSortationAllocation("code-176", "name-204")
        assertEquals(SortationAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSortationAllocation("code-176", "name-204")
        try {
            item.moveTo(SortationAllocationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationAllocationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSortationAllocation("code-176", "name-204")
        item.status = SortationAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(SortationAllocationStatus.PLANNED) }
    }
}
