package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneOverride("code-795", "label-340")
        val view = toZoneOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneOverrideViews(mutableListOf<ZoneOverride>(item, item)).size, "views")
    }
}
