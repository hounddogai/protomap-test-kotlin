package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackZoneSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackZoneSummary("code-648", "title-826")
        val view = toFallbackZoneSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackZoneSummaryViews(mutableListOf<FallbackZoneSummary>(item, item)).size, "views")
    }
}
