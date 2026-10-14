package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTransitTimeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTransitTimeScore("code-392", "name-252")
        val view = toStrictTransitTimeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTransitTimeScoreViews(mutableListOf<StrictTransitTimeScore>(item, item)).size, "views")
    }
}
