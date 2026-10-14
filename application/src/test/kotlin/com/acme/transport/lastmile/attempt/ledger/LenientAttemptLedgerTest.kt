package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAttemptLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientAttemptLedger("code-838", "title-216")
        assertEquals(LenientAttemptLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientAttemptLedger("code-838", "title-216")
        try {
            item.moveTo(LenientAttemptLedgerStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAttemptLedgerStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientAttemptLedger("code-838", "title-216")
        item.status = LenientAttemptLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientAttemptLedgerStatus.PLANNED) }
    }
}
