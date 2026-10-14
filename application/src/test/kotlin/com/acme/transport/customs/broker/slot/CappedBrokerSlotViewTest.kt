package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBrokerSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBrokerSlot("code-207", "name-151")
        val view = toCappedBrokerSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBrokerSlotViews(mutableListOf<CappedBrokerSlot>(item, item)).size, "views")
    }
}
