package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEvChargeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultEvChargeTemplate("code-114", "title-171")
        val view = toDefaultEvChargeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultEvChargeTemplateViews(mutableListOf<DefaultEvChargeTemplate>(item, item)).size, "views")
    }
}
