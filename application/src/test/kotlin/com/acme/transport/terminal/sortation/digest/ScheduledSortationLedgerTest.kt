package com.acme.transport.terminal.sortation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSortationLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledSortationLedger("code-478", "label-421")
        assertEquals(ScheduledSortationLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledSortationLedger("code-478", "label-421")
        try {
            item.moveTo(ScheduledSortationLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSortationLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledSortationLedger("code-478", "label-421")
        item.status = ScheduledSortationLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledSortationLedgerStatus.DRAFT) }
    }
}
