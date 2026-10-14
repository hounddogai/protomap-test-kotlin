package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerMilestone("code-590", "name-284")
        val view = toBrokerMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerMilestoneViews(mutableListOf<BrokerMilestone>(item, item)).size, "views")
    }
}
