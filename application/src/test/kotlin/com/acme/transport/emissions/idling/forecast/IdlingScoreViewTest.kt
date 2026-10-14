package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingScore("code-277", "label-43")
        val view = toIdlingScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingScoreViews(mutableListOf<IdlingScore>(item, item)).size, "views")
    }
}
