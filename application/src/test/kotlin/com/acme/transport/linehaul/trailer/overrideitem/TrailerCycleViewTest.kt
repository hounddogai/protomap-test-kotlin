package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerCycle("code-99", "title-364")
        val view = toTrailerCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerCycleViews(mutableListOf<TrailerCycle>(item, item)).size, "views")
    }
}
