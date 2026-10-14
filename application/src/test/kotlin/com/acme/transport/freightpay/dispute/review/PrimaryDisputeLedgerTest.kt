package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDisputeLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryDisputeLedger("code-722", "label-11")
        assertEquals(PrimaryDisputeLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryDisputeLedger("code-722", "label-11")
        try {
            item.moveTo(PrimaryDisputeLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDisputeLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryDisputeLedger("code-722", "label-11")
        item.status = PrimaryDisputeLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryDisputeLedgerStatus.CREATED) }
    }
}
