package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeMetric("code-760", "title-365")
        val view = toFuelSurchargeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeMetricViews(mutableListOf<FuelSurchargeMetric>(item, item)).size, "views")
    }
}
