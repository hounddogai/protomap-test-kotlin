package com.acme.transport.lastmile.drop.dropforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDropSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDropSegment("code-295", "name-977")
        val view = toRegionalDropSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDropSegmentViews(mutableListOf<RegionalDropSegment>(item, item)).size, "views")
    }
}
