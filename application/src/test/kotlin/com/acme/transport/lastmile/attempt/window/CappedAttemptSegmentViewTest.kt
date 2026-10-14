package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAttemptSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAttemptSegment("code-245", "title-852")
        val view = toCappedAttemptSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAttemptSegmentViews(mutableListOf<CappedAttemptSegment>(item, item)).size, "views")
    }
}
