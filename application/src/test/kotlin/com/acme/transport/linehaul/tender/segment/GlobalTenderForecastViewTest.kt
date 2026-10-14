package com.acme.transport.linehaul.tender.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTenderForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTenderForecast("code-359", "label-173")
        val view = toGlobalTenderForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTenderForecastViews(mutableListOf<GlobalTenderForecast>(item, item)).size, "views")
    }
}
