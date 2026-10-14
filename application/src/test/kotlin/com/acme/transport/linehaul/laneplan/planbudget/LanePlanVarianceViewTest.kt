package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanVariance("code-69", "title-869")
        val view = toLanePlanVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanVarianceViews(mutableListOf<LanePlanVariance>(item, item)).size, "views")
    }
}
