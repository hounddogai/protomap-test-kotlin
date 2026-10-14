package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryFuelBurnMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryFuelBurnMetric("code-283", "label-590")
        val view = toPrimaryFuelBurnMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryFuelBurnMetricViews(mutableListOf<PrimaryFuelBurnMetric>(item, item)).size, "views")
    }
}
