package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffEstimate("code-803", "name-943")
        val view = toTariffEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffEstimateViews(mutableListOf<TariffEstimate>(item, item)).size, "views")
    }
}
