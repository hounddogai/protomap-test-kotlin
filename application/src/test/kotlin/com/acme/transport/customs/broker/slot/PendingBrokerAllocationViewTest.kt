package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBrokerAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBrokerAllocation("code-716", "name-127")
        val view = toPendingBrokerAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBrokerAllocationViews(mutableListOf<PendingBrokerAllocation>(item, item)).size, "views")
    }
}
