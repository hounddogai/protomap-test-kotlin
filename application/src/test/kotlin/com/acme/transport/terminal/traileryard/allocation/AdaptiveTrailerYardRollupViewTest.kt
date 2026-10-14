package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTrailerYardRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTrailerYardRollup("code-433", "label-946")
        val view = toAdaptiveTrailerYardRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTrailerYardRollupViews(mutableListOf<AdaptiveTrailerYardRollup>(item, item)).size, "views")
    }
}
