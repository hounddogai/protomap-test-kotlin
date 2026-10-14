package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyEvChargeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyEvChargeOverride("code-74", "title-887")
        val view = toLegacyEvChargeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyEvChargeOverrideViews(mutableListOf<LegacyEvChargeOverride>(item, item)).size, "views")
    }
}
