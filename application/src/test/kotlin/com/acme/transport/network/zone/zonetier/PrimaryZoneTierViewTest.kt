package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryZoneTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryZoneTier("code-220", "title-124")
        val view = toPrimaryZoneTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryZoneTierViews(mutableListOf<PrimaryZoneTier>(item, item)).size, "views")
    }
}
