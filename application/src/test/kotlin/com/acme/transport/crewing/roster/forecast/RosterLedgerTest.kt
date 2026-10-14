package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newRosterLedger("code-235", "label-159")
        assertEquals(RosterLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newRosterLedger("code-235", "label-159")
        try {
            item.moveTo(RosterLedgerStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterLedgerStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRosterLedger("code-235", "label-159")
        item.status = RosterLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(RosterLedgerStatus.DRAFT) }
    }
}
