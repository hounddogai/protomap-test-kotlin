package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerProfile("code-182", "title-660")
        val view = toBrokerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerProfileViews(mutableListOf<BrokerProfile>(item, item)).size, "views")
    }
}
