package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTransitTimeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTransitTimeBaseline("code-814", "name-397")
        val view = toFallbackTransitTimeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTransitTimeBaselineViews(mutableListOf<FallbackTransitTimeBaseline>(item, item)).size, "views")
    }
}
