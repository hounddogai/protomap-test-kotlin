package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualViolationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualViolationForecast("code-797", "label-911")
        val view = toManualViolationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualViolationForecastViews(mutableListOf<ManualViolationForecast>(item, item)).size, "views")
    }
}
