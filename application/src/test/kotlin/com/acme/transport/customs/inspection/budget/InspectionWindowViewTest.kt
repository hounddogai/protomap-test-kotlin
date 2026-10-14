package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionWindow("code-533", "label-140")
        val view = toInspectionWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionWindowViews(mutableListOf<InspectionWindow>(item, item)).size, "views")
    }
}
