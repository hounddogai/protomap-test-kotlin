package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDefectSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDefectSlot("code-314", "name-575")
        val view = toStrictDefectSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDefectSlotViews(mutableListOf<StrictDefectSlot>(item, item)).size, "views")
    }
}
