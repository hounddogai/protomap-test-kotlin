package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerGroup("code-377", "name-93")
        val view = toBrokerGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerGroupViews(mutableListOf<BrokerGroup>(item, item)).size, "views")
    }
}
