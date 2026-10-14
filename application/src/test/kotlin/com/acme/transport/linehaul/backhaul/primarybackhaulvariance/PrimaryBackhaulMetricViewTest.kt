package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryBackhaulMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        val view = toPrimaryBackhaulMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryBackhaulMetricViews(mutableListOf<PrimaryBackhaulMetric>(item, item)).size, "views")
    }
}
