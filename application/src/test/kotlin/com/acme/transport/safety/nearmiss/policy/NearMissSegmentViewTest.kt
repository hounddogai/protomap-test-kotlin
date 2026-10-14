package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissSegment("code-101", "label-889")
        val view = toNearMissSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissSegmentViews(mutableListOf<NearMissSegment>(item, item)).size, "views")
    }
}
