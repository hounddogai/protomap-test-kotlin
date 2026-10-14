package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffLedger("code-158", "name-697")
        assertEquals(CutoffLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffLedger("code-158", "name-697")
        try {
            item.moveTo(CutoffLedgerStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffLedgerStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffLedger("code-158", "name-697")
        item.status = CutoffLedgerStatus.FAILED
        assertFails("move") { item.moveTo(CutoffLedgerStatus.NEW) }
    }
}
