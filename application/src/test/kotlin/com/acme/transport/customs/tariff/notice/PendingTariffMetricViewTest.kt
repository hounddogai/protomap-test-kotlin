package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTariffMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTariffMetric("code-738", "name-407")
        val view = toPendingTariffMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTariffMetricViews(mutableListOf<PendingTariffMetric>(item, item)).size, "views")
    }
}
