package com.acme.transport.network.transittime.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeTier("code-249", "title-611")
        val view = toTransitTimeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeTierViews(mutableListOf<TransitTimeTier>(item, item)).size, "views")
    }
}
