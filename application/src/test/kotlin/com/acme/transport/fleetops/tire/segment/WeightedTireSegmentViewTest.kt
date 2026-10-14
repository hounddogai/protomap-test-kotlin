package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTireSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTireSegment("code-266", "name-615")
        val view = toWeightedTireSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTireSegmentViews(mutableListOf<WeightedTireSegment>(item, item)).size, "views")
    }
}
