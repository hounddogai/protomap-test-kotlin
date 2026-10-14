package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerNotice("code-640", "name-282")
        val view = toLockerNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerNoticeViews(mutableListOf<LockerNotice>(item, item)).size, "views")
    }
}
