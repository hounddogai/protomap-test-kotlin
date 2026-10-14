package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakCycle("code-211", "title-28")
        val view = toRestBreakCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakCycleViews(mutableListOf<RestBreakCycle>(item, item)).size, "views")
    }
}
