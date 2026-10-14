package com.acme.transport.terminal.sortation.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackSortationRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackSortationRecord("code-251", "label-854")
        assertEquals(FallbackSortationRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFallbackSortationRecord("code-251", "label-854")
        try {
            item.moveTo(FallbackSortationRecordStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackSortationRecordStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackSortationRecord("code-251", "label-854")
        item.status = FallbackSortationRecordStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackSortationRecordStatus.DRAFT) }
    }
}
