package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationAdjustment("code-808", "label-564")
        assertEquals(SortationAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSortationAdjustment("code-808", "label-564")
        try {
            item.moveTo(SortationAdjustmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationAdjustmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationAdjustment("code-808", "label-564")
        item.status = SortationAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(SortationAdjustmentStatus.DRAFT) }
    }
}
