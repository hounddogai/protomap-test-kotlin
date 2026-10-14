package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneMapping("code-413", "name-609")
        val view = toZoneMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneMappingViews(mutableListOf<ZoneMapping>(item, item)).size, "views")
    }
}
