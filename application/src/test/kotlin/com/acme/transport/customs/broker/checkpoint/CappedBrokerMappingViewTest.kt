package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBrokerMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBrokerMapping("code-305", "label-643")
        val view = toCappedBrokerMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBrokerMappingViews(mutableListOf<CappedBrokerMapping>(item, item)).size, "views")
    }
}
