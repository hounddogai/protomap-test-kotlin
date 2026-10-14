package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRateCardRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRateCardRecord("code-945", "label-618")
        val view = toGlobalRateCardRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRateCardRecordViews(mutableListOf<GlobalRateCardRecord>(item, item)).size, "views")
    }
}
