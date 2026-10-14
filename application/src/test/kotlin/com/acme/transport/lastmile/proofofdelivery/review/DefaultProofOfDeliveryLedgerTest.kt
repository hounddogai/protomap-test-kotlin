package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultProofOfDeliveryLedger("code-414", "title-423")
        assertEquals(DefaultProofOfDeliveryLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultProofOfDeliveryLedger("code-414", "title-423")
        try {
            item.moveTo(DefaultProofOfDeliveryLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultProofOfDeliveryLedger("code-414", "title-423")
        item.status = DefaultProofOfDeliveryLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryLedgerStatus.IDLE) }
    }
}
