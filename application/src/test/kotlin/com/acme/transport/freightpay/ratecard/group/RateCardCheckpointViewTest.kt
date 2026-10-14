package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardCheckpoint("code-719", "label-913")
        val view = toRateCardCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardCheckpointViews(mutableListOf<RateCardCheckpoint>(item, item)).size, "views")
    }
}
