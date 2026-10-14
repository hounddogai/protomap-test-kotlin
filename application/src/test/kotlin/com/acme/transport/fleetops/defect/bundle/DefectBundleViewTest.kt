package com.acme.transport.fleetops.defect.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectBundle("code-454", "name-147")
        val view = toDefectBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectBundleViews(mutableListOf<DefectBundle>(item, item)).size, "views")
    }
}
