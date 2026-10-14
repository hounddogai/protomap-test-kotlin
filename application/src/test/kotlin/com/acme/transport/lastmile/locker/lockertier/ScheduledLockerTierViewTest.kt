package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLockerTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLockerTier("code-981", "title-609")
        val view = toScheduledLockerTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLockerTierViews(mutableListOf<ScheduledLockerTier>(item, item)).size, "views")
    }
}
