package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulTemplate("code-306", "name-224")
        val view = toBackhaulTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulTemplateViews(mutableListOf<BackhaulTemplate>(item, item)).size, "views")
    }
}
