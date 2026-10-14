package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class DropSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropSegment("code-899", "name-842")
        val view = toDropSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropSegmentViews(mutableListOf<DropSegment>(item, item)).size, "views")
    }
}
