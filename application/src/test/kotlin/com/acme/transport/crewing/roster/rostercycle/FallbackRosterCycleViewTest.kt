package com.acme.transport.crewing.roster.rostercycle

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRosterCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRosterCycle("code-238", "title-280")
        val view = toFallbackRosterCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRosterCycleViews(mutableListOf<FallbackRosterCycle>(item, item)).size, "views")
    }
}
