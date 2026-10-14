package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSortationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSortationTemplate("code-439", "name-728")
        val view = toTieredSortationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSortationTemplateViews(mutableListOf<TieredSortationTemplate>(item, item)).size, "views")
    }
}
