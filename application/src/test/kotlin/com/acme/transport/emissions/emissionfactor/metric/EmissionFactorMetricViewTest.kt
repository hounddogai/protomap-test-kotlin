package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorMetric("code-845", "label-41")
        val view = toEmissionFactorMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorMetricViews(mutableListOf<EmissionFactorMetric>(item, item)).size, "views")
    }
}
