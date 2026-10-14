package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredQualificationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredQualificationMetric("code-381", "title-351")
        val view = toTieredQualificationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredQualificationMetricViews(mutableListOf<TieredQualificationMetric>(item, item)).size, "views")
    }
}
