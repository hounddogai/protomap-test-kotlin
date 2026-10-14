package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingBrokerSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingBrokerSlot("code-606", "name-539")
        val view = toRollingBrokerSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingBrokerSlotViews(mutableListOf<RollingBrokerSlot>(item, item)).size, "views")
    }
}
