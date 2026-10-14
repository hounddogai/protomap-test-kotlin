package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryAllocation("code-650", "name-466")
        val view = toRedeliveryAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryAllocationViews(mutableListOf<RedeliveryAllocation>(item, item)).size, "views")
    }
}
