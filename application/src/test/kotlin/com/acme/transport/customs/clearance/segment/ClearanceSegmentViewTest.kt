package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceSegment("code-974", "title-584")
        val view = toClearanceSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceSegmentViews(mutableListOf<ClearanceSegment>(item, item)).size, "views")
    }
}
