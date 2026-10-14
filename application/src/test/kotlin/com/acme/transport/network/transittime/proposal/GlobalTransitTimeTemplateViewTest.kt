package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTransitTimeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTransitTimeTemplate("code-680", "title-424")
        val view = toGlobalTransitTimeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTransitTimeTemplateViews(mutableListOf<GlobalTransitTimeTemplate>(item, item)).size, "views")
    }
}
