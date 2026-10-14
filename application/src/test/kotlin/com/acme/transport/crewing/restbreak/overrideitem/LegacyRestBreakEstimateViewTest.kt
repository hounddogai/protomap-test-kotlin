package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRestBreakEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRestBreakEstimate("code-203", "title-533")
        val view = toLegacyRestBreakEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRestBreakEstimateViews(mutableListOf<LegacyRestBreakEstimate>(item, item)).size, "views")
    }
}
