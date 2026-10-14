package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRedeliveryNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRedeliveryNotice("code-224", "title-900")
        val view = toScheduledRedeliveryNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRedeliveryNoticeViews(mutableListOf<ScheduledRedeliveryNotice>(item, item)).size, "views")
    }
}
