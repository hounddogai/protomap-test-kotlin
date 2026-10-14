package com.acme.transport.fleetops.utilization.utilizationoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalUtilizationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalUtilizationOverride("code-996", "name-797")
        val view = toRegionalUtilizationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalUtilizationOverrideViews(mutableListOf<RegionalUtilizationOverride>(item, item)).size, "views")
    }
}
