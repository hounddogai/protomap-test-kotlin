package com.acme.transport.crewing.qualification.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingQualificationMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingQualificationMetric("code-268", "label-760")
        val view = toPendingQualificationMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingQualificationMetricViews(mutableListOf<PendingQualificationMetric>(item, item)).size, "views")
    }
}
