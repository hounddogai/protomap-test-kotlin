package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHoldNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHoldNotice("code-231", "name-72")
        val view = toScheduledHoldNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHoldNoticeViews(mutableListOf<ScheduledHoldNotice>(item, item)).size, "views")
    }
}
