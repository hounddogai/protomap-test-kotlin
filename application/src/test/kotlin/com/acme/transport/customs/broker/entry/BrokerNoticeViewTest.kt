package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerNotice("code-267", "label-68")
        val view = toBrokerNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerNoticeViews(mutableListOf<BrokerNotice>(item, item)).size, "views")
    }
}
