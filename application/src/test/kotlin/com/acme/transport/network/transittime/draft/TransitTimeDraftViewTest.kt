package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeDraft("code-865", "name-507")
        val view = toTransitTimeDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeDraftViews(mutableListOf<TransitTimeDraft>(item, item)).size, "views")
    }
}
