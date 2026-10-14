package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAccessorialMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        val view = toSeasonalAccessorialMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAccessorialMetricViews(mutableListOf<SeasonalAccessorialMetric>(item, item)).size, "views")
    }
}
