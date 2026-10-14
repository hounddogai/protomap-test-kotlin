package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredClearanceDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredClearanceDraft("code-741", "title-193")
        val view = toTieredClearanceDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredClearanceDraftViews(mutableListOf<TieredClearanceDraft>(item, item)).size, "views")
    }
}
