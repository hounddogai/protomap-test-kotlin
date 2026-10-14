package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationMapping("code-210", "label-484")
        val view = toUtilizationMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationMappingViews(mutableListOf<UtilizationMapping>(item, item)).size, "views")
    }
}
