package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCutoffLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackCutoffLedger("code-919", "title-163")
        assertEquals(FallbackCutoffLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFallbackCutoffLedger("code-919", "title-163")
        try {
            item.moveTo(FallbackCutoffLedgerStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCutoffLedgerStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackCutoffLedger("code-919", "title-163")
        item.status = FallbackCutoffLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackCutoffLedgerStatus.CREATED) }
    }
}
