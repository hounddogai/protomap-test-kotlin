package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAccessorialLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        assertEquals(FallbackAccessorialLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        try {
            item.moveTo(FallbackAccessorialLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAccessorialLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        item.status = FallbackAccessorialLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackAccessorialLedgerStatus.DRAFT) }
    }
}
