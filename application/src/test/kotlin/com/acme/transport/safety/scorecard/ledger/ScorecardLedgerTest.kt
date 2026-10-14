package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newScorecardLedger("code-816", "title-895")
        assertEquals(ScorecardLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScorecardLedger("code-816", "title-895")
        try {
            item.moveTo(ScorecardLedgerStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardLedgerStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScorecardLedger("code-816", "title-895")
        item.status = ScorecardLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(ScorecardLedgerStatus.DRAFT) }
    }
}
