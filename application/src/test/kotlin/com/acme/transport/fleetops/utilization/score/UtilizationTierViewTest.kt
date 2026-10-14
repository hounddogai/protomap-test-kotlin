package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationTier("code-861", "label-569")
        val view = toUtilizationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationTierViews(mutableListOf<UtilizationTier>(item, item)).size, "views")
    }
}
