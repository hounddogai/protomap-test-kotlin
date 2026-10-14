package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeClaim("code-391", "name-958")
        val view = toTransitTimeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeClaimViews(mutableListOf<TransitTimeClaim>(item, item)).size, "views")
    }
}
