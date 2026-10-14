package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSortationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSortationMetric("code-859", "title-608")
        val view = toTieredSortationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSortationMetricViews(mutableListOf<TieredSortationMetric>(item, item)).size, "views")
    }
}
