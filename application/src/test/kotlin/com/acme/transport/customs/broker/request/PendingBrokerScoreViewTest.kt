package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBrokerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBrokerScore("code-546", "name-706")
        val view = toPendingBrokerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBrokerScoreViews(mutableListOf<PendingBrokerScore>(item, item)).size, "views")
    }
}
