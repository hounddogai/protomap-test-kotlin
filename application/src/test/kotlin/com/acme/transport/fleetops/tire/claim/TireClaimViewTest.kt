package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TireClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireClaim("code-992", "label-798")
        val view = toTireClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireClaimViews(mutableListOf<TireClaim>(item, item)).size, "views")
    }
}
