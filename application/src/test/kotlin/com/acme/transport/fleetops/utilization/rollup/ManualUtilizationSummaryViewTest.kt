package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualUtilizationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualUtilizationSummary("code-781", "name-208")
        val view = toManualUtilizationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualUtilizationSummaryViews(mutableListOf<ManualUtilizationSummary>(item, item)).size, "views")
    }
}
