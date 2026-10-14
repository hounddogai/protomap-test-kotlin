package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionBundle("code-595", "title-128")
        val view = toInspectionBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionBundleViews(mutableListOf<InspectionBundle>(item, item)).size, "views")
    }
}
