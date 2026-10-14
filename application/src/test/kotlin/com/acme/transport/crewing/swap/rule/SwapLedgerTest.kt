package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newSwapLedger("code-905", "title-217")
        assertEquals(SwapLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSwapLedger("code-905", "title-217")
        try {
            item.moveTo(SwapLedgerStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapLedgerStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSwapLedger("code-905", "title-217")
        item.status = SwapLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(SwapLedgerStatus.REQUESTED) }
    }
}
