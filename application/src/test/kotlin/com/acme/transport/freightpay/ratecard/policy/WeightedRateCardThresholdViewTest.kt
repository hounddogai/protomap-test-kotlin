package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRateCardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        val view = toWeightedRateCardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRateCardThresholdViews(mutableListOf<WeightedRateCardThreshold>(item, item)).size, "views")
    }
}
