package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLockerClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        val view = toAdaptiveLockerClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLockerClaimViews(mutableListOf<AdaptiveLockerClaim>(item, item)).size, "views")
    }
}
