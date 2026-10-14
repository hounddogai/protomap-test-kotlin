package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalIdlingScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalIdlingScore("code-559", "name-984")
        val view = toGlobalIdlingScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalIdlingScoreViews(mutableListOf<GlobalIdlingScore>(item, item)).size, "views")
    }
}
