package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingZoneWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingZoneWindow("code-991", "title-558")
        val view = toRollingZoneWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingZoneWindowViews(mutableListOf<RollingZoneWindow>(item, item)).size, "views")
    }
}
