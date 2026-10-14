package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderForecast("code-125", "title-537")
        val view = toTenderForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderForecastViews(mutableListOf<TenderForecast>(item, item)).size, "views")
    }
}
