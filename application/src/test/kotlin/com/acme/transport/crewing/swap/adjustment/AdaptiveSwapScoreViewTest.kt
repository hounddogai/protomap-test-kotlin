package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveSwapScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveSwapScore("code-18", "label-991")
        val view = toAdaptiveSwapScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveSwapScoreViews(mutableListOf<AdaptiveSwapScore>(item, item)).size, "views")
    }
}
