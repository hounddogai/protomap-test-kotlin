package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingSegment("code-454", "name-83")
        val view = toIdlingSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingSegmentViews(mutableListOf<IdlingSegment>(item, item)).size, "views")
    }
}
