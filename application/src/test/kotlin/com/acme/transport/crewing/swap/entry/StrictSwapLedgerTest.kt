package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSwapLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictSwapLedger("code-917", "name-892")
        assertEquals(StrictSwapLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newStrictSwapLedger("code-917", "name-892")
        try {
            item.moveTo(StrictSwapLedgerStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSwapLedgerStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictSwapLedger("code-917", "name-892")
        item.status = StrictSwapLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictSwapLedgerStatus.PROPOSED) }
    }
}
