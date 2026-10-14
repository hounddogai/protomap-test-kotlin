package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRosterSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRosterSegment("code-184", "name-594")
        val view = toRegionalRosterSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRosterSegmentViews(mutableListOf<RegionalRosterSegment>(item, item)).size, "views")
    }
}
