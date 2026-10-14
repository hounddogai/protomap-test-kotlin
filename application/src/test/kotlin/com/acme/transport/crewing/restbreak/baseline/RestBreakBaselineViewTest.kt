package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakBaseline("code-636", "name-173")
        val view = toRestBreakBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakBaselineViews(mutableListOf<RestBreakBaseline>(item, item)).size, "views")
    }
}
