package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyQualificationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyQualificationMetric("code-817", "label-793")
        val view = toLegacyQualificationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyQualificationMetricViews(mutableListOf<LegacyQualificationMetric>(item, item)).size, "views")
    }
}
