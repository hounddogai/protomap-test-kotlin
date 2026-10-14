package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneException("code-4", "label-384")
        val view = toZoneExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneExceptionViews(mutableListOf<ZoneException>(item, item)).size, "views")
    }
}
