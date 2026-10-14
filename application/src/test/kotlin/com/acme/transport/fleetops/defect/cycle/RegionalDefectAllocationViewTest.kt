package com.acme.transport.fleetops.defect.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDefectAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDefectAllocation("code-296", "name-418")
        val view = toRegionalDefectAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDefectAllocationViews(mutableListOf<RegionalDefectAllocation>(item, item)).size, "views")
    }
}
