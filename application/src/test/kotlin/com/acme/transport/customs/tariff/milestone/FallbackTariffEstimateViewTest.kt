package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTariffEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTariffEstimate("code-951", "title-621")
        val view = toFallbackTariffEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTariffEstimateViews(mutableListOf<FallbackTariffEstimate>(item, item)).size, "views")
    }
}
