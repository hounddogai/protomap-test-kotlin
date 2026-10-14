package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRateCardScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRateCardSchedule("code-332", "label-545")
        val view = toRollingRateCardScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRateCardScheduleViews(mutableListOf<RollingRateCardSchedule>(item, item)).size, "views")
    }
}
