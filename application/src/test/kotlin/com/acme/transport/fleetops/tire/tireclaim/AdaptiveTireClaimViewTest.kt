package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTireClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTireClaim("code-848", "title-794")
        val view = toAdaptiveTireClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTireClaimViews(mutableListOf<AdaptiveTireClaim>(item, item)).size, "views")
    }
}
