package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryMapping("code-907", "name-333")
        val view = toRedeliveryMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryMappingViews(mutableListOf<RedeliveryMapping>(item, item)).size, "views")
    }
}
