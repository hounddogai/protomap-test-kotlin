package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNearMissClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        val view = toAdaptiveNearMissClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNearMissClaimViews(mutableListOf<AdaptiveNearMissClaim>(item, item)).size, "views")
    }
}
