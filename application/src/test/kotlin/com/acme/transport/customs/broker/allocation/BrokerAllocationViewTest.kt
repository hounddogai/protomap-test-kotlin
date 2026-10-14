package com.acme.transport.customs.broker.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerAllocation("code-978", "title-48")
        val view = toBrokerAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerAllocationViews(mutableListOf<BrokerAllocation>(item, item)).size, "views")
    }
}
