package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAccessorialNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAccessorialNotice("code-155", "name-618")
        val view = toScheduledAccessorialNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAccessorialNoticeViews(mutableListOf<ScheduledAccessorialNotice>(item, item)).size, "views")
    }
}
