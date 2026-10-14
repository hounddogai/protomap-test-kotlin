package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyUtilizationSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        val view = toLegacyUtilizationSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyUtilizationSlotViews(mutableListOf<LegacyUtilizationSlot>(item, item)).size, "views")
    }
}
