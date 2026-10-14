package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalInspectionTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalInspectionTarget("code-266", "name-871")
        val view = toRegionalInspectionTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalInspectionTargetViews(mutableListOf<RegionalInspectionTarget>(item, item)).size, "views")
    }
}
