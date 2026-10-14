package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionMetric("code-581", "label-348")
        val view = toInspectionMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionMetricViews(mutableListOf<InspectionMetric>(item, item)).size, "views")
    }
}
