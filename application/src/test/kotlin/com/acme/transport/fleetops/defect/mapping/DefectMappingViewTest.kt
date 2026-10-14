package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectMapping("code-774", "label-296")
        val view = toDefectMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectMappingViews(mutableListOf<DefectMapping>(item, item)).size, "views")
    }
}
