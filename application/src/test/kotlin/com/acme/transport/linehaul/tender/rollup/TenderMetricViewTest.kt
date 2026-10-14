package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderMetric("code-974", "label-651")
        val view = toTenderMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderMetricViews(mutableListOf<TenderMetric>(item, item)).size, "views")
    }
}
