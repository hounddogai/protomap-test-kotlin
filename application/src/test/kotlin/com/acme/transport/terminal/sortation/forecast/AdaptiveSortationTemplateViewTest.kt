package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveSortationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        val view = toAdaptiveSortationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveSortationTemplateViews(mutableListOf<AdaptiveSortationTemplate>(item, item)).size, "views")
    }
}
