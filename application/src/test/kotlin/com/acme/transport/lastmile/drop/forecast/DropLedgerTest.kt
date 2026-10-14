package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newDropLedger("code-30", "name-982")
        assertEquals(DropLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDropLedger("code-30", "name-982")
        try {
            item.moveTo(DropLedgerStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropLedgerStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDropLedger("code-30", "name-982")
        item.status = DropLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(DropLedgerStatus.DRAFT) }
    }
}
