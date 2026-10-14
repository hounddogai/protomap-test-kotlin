package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveFuelBurnClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveFuelBurnClaim("code-644", "label-630")
        val view = toAdaptiveFuelBurnClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveFuelBurnClaimViews(mutableListOf<AdaptiveFuelBurnClaim>(item, item)).size, "views")
    }
}
