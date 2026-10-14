package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalInspectionWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalInspectionWindow("code-995", "title-717")
        val view = toGlobalInspectionWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalInspectionWindowViews(mutableListOf<GlobalInspectionWindow>(item, item)).size, "views")
    }
}
