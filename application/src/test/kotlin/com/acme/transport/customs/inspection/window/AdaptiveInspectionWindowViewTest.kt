package com.acme.transport.customs.inspection.window

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionWindow("code-853", "label-558")
        val view = toAdaptiveInspectionWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionWindowViews(mutableListOf<AdaptiveInspectionWindow>(item, item)).size, "views")
    }
}
