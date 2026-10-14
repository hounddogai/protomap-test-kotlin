package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneDraft("code-802", "label-662")
        val view = toZoneDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneDraftViews(mutableListOf<ZoneDraft>(item, item)).size, "views")
    }
}
