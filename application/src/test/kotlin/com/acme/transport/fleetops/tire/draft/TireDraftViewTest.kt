package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class TireDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireDraft("code-138", "label-776")
        val view = toTireDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireDraftViews(mutableListOf<TireDraft>(item, item)).size, "views")
    }
}
