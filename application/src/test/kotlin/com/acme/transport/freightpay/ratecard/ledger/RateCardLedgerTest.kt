package com.acme.transport.freightpay.ratecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newRateCardLedger("code-595", "title-846")
        assertEquals(RateCardLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRateCardLedger("code-595", "title-846")
        try {
            item.moveTo(RateCardLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRateCardLedger("code-595", "title-846")
        item.status = RateCardLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(RateCardLedgerStatus.CREATED) }
    }
}
