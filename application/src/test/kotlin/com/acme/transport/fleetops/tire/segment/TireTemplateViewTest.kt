package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class TireTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireTemplate("code-212", "name-340")
        val view = toTireTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireTemplateViews(mutableListOf<TireTemplate>(item, item)).size, "views")
    }
}
