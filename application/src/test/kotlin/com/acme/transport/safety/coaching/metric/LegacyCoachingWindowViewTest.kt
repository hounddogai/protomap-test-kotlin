package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCoachingWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCoachingWindow("code-919", "label-816")
        val view = toLegacyCoachingWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCoachingWindowViews(mutableListOf<LegacyCoachingWindow>(item, item)).size, "views")
    }
}
