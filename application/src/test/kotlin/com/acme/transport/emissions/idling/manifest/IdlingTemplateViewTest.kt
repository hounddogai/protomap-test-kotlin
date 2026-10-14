package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingTemplate("code-261", "label-841")
        val view = toIdlingTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingTemplateViews(mutableListOf<IdlingTemplate>(item, item)).size, "views")
    }
}
