package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedParcelForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedParcelForecast("code-107", "title-999")
        val view = toBatchedParcelForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedParcelForecastViews(mutableListOf<BatchedParcelForecast>(item, item)).size, "views")
    }
}
