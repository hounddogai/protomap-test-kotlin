package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellTemplate("code-536", "name-692")
        val view = toDwellTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellTemplateViews(mutableListOf<DwellTemplate>(item, item)).size, "views")
    }
}
