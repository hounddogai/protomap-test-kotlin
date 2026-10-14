package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRedeliveryNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        val view = toDefaultRedeliveryNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRedeliveryNoticeViews(mutableListOf<DefaultRedeliveryNotice>(item, item)).size, "views")
    }
}
