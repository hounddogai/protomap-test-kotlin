package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBrokerSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBrokerSummary("code-188", "name-298")
        val view = toGlobalBrokerSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBrokerSummaryViews(mutableListOf<GlobalBrokerSummary>(item, item)).size, "views")
    }
}
