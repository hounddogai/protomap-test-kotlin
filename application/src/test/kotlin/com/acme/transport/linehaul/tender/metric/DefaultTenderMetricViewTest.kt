package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTenderMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTenderMetric("code-382", "label-813")
        val view = toDefaultTenderMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTenderMetricViews(mutableListOf<DefaultTenderMetric>(item, item)).size, "views")
    }
}
