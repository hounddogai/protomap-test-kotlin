package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHubLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingHubLedger("code-943", "label-352")
        assertEquals(PendingHubLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingHubLedger("code-943", "label-352")
        try {
            item.moveTo(PendingHubLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHubLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingHubLedger("code-943", "label-352")
        item.status = PendingHubLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingHubLedgerStatus.PROPOSED) }
    }
}
