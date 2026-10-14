package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLockerSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLockerSegment("code-395", "label-848")
        val view = toCappedLockerSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLockerSegmentViews(mutableListOf<CappedLockerSegment>(item, item)).size, "views")
    }
}
