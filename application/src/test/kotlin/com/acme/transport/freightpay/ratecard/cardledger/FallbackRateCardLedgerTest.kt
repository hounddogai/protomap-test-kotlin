package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRateCardLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackRateCardLedger("code-154", "title-968")
        assertEquals(FallbackRateCardLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newFallbackRateCardLedger("code-154", "title-968")
        try {
            item.moveTo(FallbackRateCardLedgerStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRateCardLedgerStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackRateCardLedger("code-154", "title-968")
        item.status = FallbackRateCardLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackRateCardLedgerStatus.PLANNED) }
    }
}
