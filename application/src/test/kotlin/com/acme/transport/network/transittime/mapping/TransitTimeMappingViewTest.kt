package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeMapping("code-361", "title-886")
        val view = toTransitTimeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeMappingViews(mutableListOf<TransitTimeMapping>(item, item)).size, "views")
    }
}
