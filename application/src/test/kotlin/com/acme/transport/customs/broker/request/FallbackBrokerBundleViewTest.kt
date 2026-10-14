package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackBrokerBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackBrokerBundle("code-180", "name-432")
        val view = toFallbackBrokerBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackBrokerBundleViews(mutableListOf<FallbackBrokerBundle>(item, item)).size, "views")
    }
}
