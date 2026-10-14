package com.acme.transport.customs.broker.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerBundle("code-594", "name-131")
        val view = toBrokerBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerBundleViews(mutableListOf<BrokerBundle>(item, item)).size, "views")
    }
}
