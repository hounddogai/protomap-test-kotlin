package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCoachingLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingCoachingLedger("code-292", "title-430")
        assertEquals(PendingCoachingLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingCoachingLedger("code-292", "title-430")
        try {
            item.moveTo(PendingCoachingLedgerStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCoachingLedgerStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingCoachingLedger("code-292", "title-430")
        item.status = PendingCoachingLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingCoachingLedgerStatus.PLANNED) }
    }
}
