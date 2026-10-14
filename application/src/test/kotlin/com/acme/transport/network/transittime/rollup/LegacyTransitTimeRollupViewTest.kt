package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTransitTimeRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTransitTimeRollup("code-265", "label-126")
        val view = toLegacyTransitTimeRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTransitTimeRollupViews(mutableListOf<LegacyTransitTimeRollup>(item, item)).size, "views")
    }
}
