package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationLedger("code-575", "title-185")
        assertEquals(SortationLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSortationLedger("code-575", "title-185")
        try {
            item.moveTo(SortationLedgerStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationLedgerStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationLedger("code-575", "title-185")
        item.status = SortationLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationLedgerStatus.REQUESTED) }
    }
}
