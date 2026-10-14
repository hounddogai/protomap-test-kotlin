package com.acme.transport.customs.clearance.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceMapping("code-913", "title-322")
        val view = toClearanceMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceMappingViews(mutableListOf<ClearanceMapping>(item, item)).size, "views")
    }
}
