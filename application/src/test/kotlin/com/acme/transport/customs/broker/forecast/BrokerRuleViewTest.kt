package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerRule("code-383", "name-71")
        val view = toBrokerRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerRuleViews(mutableListOf<BrokerRule>(item, item)).size, "views")
    }
}
