package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictViolationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictViolationForecast("code-163", "title-938")
        val view = toStrictViolationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictViolationForecastViews(mutableListOf<StrictViolationForecast>(item, item)).size, "views")
    }
}
