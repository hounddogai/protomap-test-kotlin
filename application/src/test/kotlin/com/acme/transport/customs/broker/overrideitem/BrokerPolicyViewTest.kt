package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerPolicy("code-258", "title-96")
        val view = toBrokerPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerPolicyViews(mutableListOf<BrokerPolicy>(item, item)).size, "views")
    }
}
