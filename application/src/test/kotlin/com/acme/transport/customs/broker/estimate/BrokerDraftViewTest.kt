package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerDraft("code-109", "label-310")
        val view = toBrokerDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerDraftViews(mutableListOf<BrokerDraft>(item, item)).size, "views")
    }
}
