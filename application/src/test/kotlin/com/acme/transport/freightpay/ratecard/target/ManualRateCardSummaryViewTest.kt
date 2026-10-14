package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRateCardSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRateCardSummary("code-353", "label-888")
        val view = toManualRateCardSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRateCardSummaryViews(mutableListOf<ManualRateCardSummary>(item, item)).size, "views")
    }
}
