package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTenderWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTenderWindow("code-275", "title-567")
        val view = toAdaptiveTenderWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTenderWindowViews(mutableListOf<AdaptiveTenderWindow>(item, item)).size, "views")
    }
}
