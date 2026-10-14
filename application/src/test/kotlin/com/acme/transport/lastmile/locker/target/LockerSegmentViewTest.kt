package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerSegment("code-309", "label-646")
        val view = toLockerSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerSegmentViews(mutableListOf<LockerSegment>(item, item)).size, "views")
    }
}
