package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNearMissSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNearMissSummary("code-556", "name-568")
        val view = toAdaptiveNearMissSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNearMissSummaryViews(mutableListOf<AdaptiveNearMissSummary>(item, item)).size, "views")
    }
}
