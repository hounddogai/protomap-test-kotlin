package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerTier("code-262", "name-335")
        val view = toBrokerTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerTierViews(mutableListOf<BrokerTier>(item, item)).size, "views")
    }
}
