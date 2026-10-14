package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceScore("code-785", "label-165")
        val view = toClearanceScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceScoreViews(mutableListOf<ClearanceScore>(item, item)).size, "views")
    }
}
