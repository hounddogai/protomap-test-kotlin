package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerMapping("code-636", "title-48")
        val view = toBrokerMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerMappingViews(mutableListOf<BrokerMapping>(item, item)).size, "views")
    }
}
