package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalViolationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalViolationSegment("code-169", "title-248")
        val view = toGlobalViolationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalViolationSegmentViews(mutableListOf<GlobalViolationSegment>(item, item)).size, "views")
    }
}
