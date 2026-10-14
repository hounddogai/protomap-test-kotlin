package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerSummary("code-279", "name-202")
        val view = toBrokerSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerSummaryViews(mutableListOf<BrokerSummary>(item, item)).size, "views")
    }
}
