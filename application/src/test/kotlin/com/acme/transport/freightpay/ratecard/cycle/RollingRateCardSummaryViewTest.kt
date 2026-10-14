package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRateCardSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRateCardSummary("code-311", "label-851")
        val view = toRollingRateCardSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRateCardSummaryViews(mutableListOf<RollingRateCardSummary>(item, item)).size, "views")
    }
}
