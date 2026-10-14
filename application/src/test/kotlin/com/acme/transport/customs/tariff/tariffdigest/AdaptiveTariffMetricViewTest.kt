package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTariffMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        val view = toAdaptiveTariffMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTariffMetricViews(mutableListOf<AdaptiveTariffMetric>(item, item)).size, "views")
    }
}
