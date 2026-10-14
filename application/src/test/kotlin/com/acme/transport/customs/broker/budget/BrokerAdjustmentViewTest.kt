package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerAdjustment("code-82", "title-533")
        val view = toBrokerAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerAdjustmentViews(mutableListOf<BrokerAdjustment>(item, item)).size, "views")
    }
}
