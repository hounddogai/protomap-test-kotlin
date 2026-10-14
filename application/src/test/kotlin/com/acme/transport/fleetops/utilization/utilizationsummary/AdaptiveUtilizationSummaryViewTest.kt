package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveUtilizationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveUtilizationSummary("code-34", "name-565")
        val view = toAdaptiveUtilizationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveUtilizationSummaryViews(mutableListOf<AdaptiveUtilizationSummary>(item, item)).size, "views")
    }
}
