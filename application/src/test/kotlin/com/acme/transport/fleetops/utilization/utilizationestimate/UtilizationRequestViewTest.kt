package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationRequest("code-931", "label-952")
        val view = toUtilizationRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationRequestViews(mutableListOf<UtilizationRequest>(item, item)).size, "views")
    }
}
