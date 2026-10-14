package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationCycle("code-527", "label-687")
        val view = toUtilizationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationCycleViews(mutableListOf<UtilizationCycle>(item, item)).size, "views")
    }
}
