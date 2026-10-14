package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRateCardScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRateCardSchedule("code-675", "name-958")
        val view = toCappedRateCardScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRateCardScheduleViews(mutableListOf<CappedRateCardSchedule>(item, item)).size, "views")
    }
}
