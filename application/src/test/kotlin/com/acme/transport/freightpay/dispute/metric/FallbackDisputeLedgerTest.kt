package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDisputeLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackDisputeLedger("code-815", "name-803")
        assertEquals(FallbackDisputeLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFallbackDisputeLedger("code-815", "name-803")
        try {
            item.moveTo(FallbackDisputeLedgerStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDisputeLedgerStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackDisputeLedger("code-815", "name-803")
        item.status = FallbackDisputeLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackDisputeLedgerStatus.DRAFT) }
    }
}
