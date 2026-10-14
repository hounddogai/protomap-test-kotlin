package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryRequest("code-670", "name-256")
        val view = toRedeliveryRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryRequestViews(mutableListOf<RedeliveryRequest>(item, item)).size, "views")
    }
}
