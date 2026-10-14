package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerRollup("code-709", "name-79")
        val view = toBrokerRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerRollupViews(mutableListOf<BrokerRollup>(item, item)).size, "views")
    }
}
