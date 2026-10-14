package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRateCardBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRateCardBaseline("code-213", "title-122")
        val view = toScheduledRateCardBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRateCardBaselineViews(mutableListOf<ScheduledRateCardBaseline>(item, item)).size, "views")
    }
}
