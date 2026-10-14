package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceTemplate("code-256", "title-71")
        val view = toClearanceTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceTemplateViews(mutableListOf<ClearanceTemplate>(item, item)).size, "views")
    }
}
