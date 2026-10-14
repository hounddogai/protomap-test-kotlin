package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryNotice("code-16", "label-711")
        val view = toRedeliveryNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryNoticeViews(mutableListOf<RedeliveryNotice>(item, item)).size, "views")
    }
}
