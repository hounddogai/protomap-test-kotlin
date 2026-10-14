package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTenderForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTenderForecast("code-304", "label-138")
        val view = toBatchedTenderForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTenderForecastViews(mutableListOf<BatchedTenderForecast>(item, item)).size, "views")
    }
}
