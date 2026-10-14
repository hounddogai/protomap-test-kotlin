package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredNodeLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newTieredNodeLedger("code-70", "label-126")
        assertEquals(TieredNodeLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredNodeLedger("code-70", "label-126")
        try {
            item.moveTo(TieredNodeLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredNodeLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredNodeLedger("code-70", "label-126")
        item.status = TieredNodeLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(TieredNodeLedgerStatus.CREATED) }
    }
}
