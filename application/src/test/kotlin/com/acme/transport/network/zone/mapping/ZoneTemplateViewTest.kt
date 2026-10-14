package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneTemplate("code-577", "name-159")
        val view = toZoneTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneTemplateViews(mutableListOf<ZoneTemplate>(item, item)).size, "views")
    }
}
