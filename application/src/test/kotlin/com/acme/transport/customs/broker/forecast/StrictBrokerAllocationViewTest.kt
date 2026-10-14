package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBrokerAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBrokerAllocation("code-828", "title-526")
        val view = toStrictBrokerAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBrokerAllocationViews(mutableListOf<StrictBrokerAllocation>(item, item)).size, "views")
    }
}
