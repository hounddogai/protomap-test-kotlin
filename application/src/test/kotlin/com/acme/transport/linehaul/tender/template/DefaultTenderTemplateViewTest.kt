package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTenderTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTenderTemplate("code-784", "name-683")
        val view = toDefaultTenderTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTenderTemplateViews(mutableListOf<DefaultTenderTemplate>(item, item)).size, "views")
    }
}
