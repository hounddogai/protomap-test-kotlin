package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTenderTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTenderTemplate("code-12", "label-638")
        val view = toCappedTenderTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTenderTemplateViews(mutableListOf<CappedTenderTemplate>(item, item)).size, "views")
    }
}
