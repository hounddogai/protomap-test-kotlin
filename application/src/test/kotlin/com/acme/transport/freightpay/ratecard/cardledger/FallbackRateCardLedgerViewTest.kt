package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRateCardLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRateCardLedger("code-154", "title-968")
        val view = toFallbackRateCardLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRateCardLedgerViews(mutableListOf<FallbackRateCardLedger>(item, item)).size, "views")
    }
}
