package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRestBreakScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRestBreakScore("code-748", "label-922")
        val view = toAdaptiveRestBreakScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRestBreakScoreViews(mutableListOf<AdaptiveRestBreakScore>(item, item)).size, "views")
    }
}
