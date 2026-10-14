package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRateCardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRateCardThreshold("code-704", "name-965")
        val view = toBatchedRateCardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRateCardThresholdViews(mutableListOf<BatchedRateCardThreshold>(item, item)).size, "views")
    }
}
