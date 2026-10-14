package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSwapMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalSwapMetric("code-533", "title-933")
        val view = toGlobalSwapMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalSwapMetricViews(mutableListOf<GlobalSwapMetric>(item, item)).size, "views")
    }
}
