package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveUtilizationRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveUtilizationRollup("code-814", "name-539")
        val view = toAdaptiveUtilizationRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveUtilizationRollupViews(mutableListOf<AdaptiveUtilizationRollup>(item, item)).size, "views")
    }
}
