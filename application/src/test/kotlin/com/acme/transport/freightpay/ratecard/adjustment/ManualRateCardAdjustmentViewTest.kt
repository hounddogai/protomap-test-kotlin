package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRateCardAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRateCardAdjustment("code-252", "title-318")
        val view = toManualRateCardAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRateCardAdjustmentViews(mutableListOf<ManualRateCardAdjustment>(item, item)).size, "views")
    }
}
