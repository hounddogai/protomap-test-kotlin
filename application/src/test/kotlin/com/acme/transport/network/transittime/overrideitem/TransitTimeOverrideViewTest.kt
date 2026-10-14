package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeOverride("code-380", "name-18")
        val view = toTransitTimeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeOverrideViews(mutableListOf<TransitTimeOverride>(item, item)).size, "views")
    }
}
