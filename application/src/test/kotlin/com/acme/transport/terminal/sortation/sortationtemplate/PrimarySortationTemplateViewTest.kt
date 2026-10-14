package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimarySortationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimarySortationTemplate("code-599", "title-620")
        val view = toPrimarySortationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimarySortationTemplateViews(mutableListOf<PrimarySortationTemplate>(item, item)).size, "views")
    }
}
