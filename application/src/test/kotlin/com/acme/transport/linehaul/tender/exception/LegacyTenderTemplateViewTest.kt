package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTenderTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTenderTemplate("code-643", "label-881")
        val view = toLegacyTenderTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTenderTemplateViews(mutableListOf<LegacyTenderTemplate>(item, item)).size, "views")
    }
}
