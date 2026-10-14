package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalZoneClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalZoneClaim("code-259", "title-692")
        val view = toGlobalZoneClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalZoneClaimViews(mutableListOf<GlobalZoneClaim>(item, item)).size, "views")
    }
}
