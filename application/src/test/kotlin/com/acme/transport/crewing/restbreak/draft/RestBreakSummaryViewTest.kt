package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakSummary("code-705", "name-506")
        val view = toRestBreakSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakSummaryViews(mutableListOf<RestBreakSummary>(item, item)).size, "views")
    }
}
