package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeProfile("code-790", "name-23")
        val view = toTransitTimeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeProfileViews(mutableListOf<TransitTimeProfile>(item, item)).size, "views")
    }
}
