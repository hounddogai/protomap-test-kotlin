package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRateCardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRateCardThreshold("code-991", "label-413")
        val view = toStrictRateCardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRateCardThresholdViews(mutableListOf<StrictRateCardThreshold>(item, item)).size, "views")
    }
}
