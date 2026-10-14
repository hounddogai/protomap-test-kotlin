package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeTemplate("code-714", "name-993")
        val view = toTransitTimeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeTemplateViews(mutableListOf<TransitTimeTemplate>(item, item)).size, "views")
    }
}
