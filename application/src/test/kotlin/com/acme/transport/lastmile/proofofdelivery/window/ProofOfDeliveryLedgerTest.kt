package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryLedger("code-319", "title-871")
        assertEquals(ProofOfDeliveryLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newProofOfDeliveryLedger("code-319", "title-871")
        try {
            item.moveTo(ProofOfDeliveryLedgerStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryLedgerStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryLedger("code-319", "title-871")
        item.status = ProofOfDeliveryLedgerStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryLedgerStatus.NEW) }
    }
}
