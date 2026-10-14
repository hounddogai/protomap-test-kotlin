package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerSummary("code-413", "name-693")
        val view = toLockerSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerSummaryViews(mutableListOf<LockerSummary>(item, item)).size, "views")
    }
}
