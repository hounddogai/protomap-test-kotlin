package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerEntry("code-35", "name-530")
        val view = toBrokerEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerEntryViews(mutableListOf<BrokerEntry>(item, item)).size, "views")
    }
}
