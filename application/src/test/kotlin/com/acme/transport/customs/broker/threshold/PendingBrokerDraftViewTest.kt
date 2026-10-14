package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBrokerDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBrokerDraft("code-305", "label-273")
        val view = toPendingBrokerDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBrokerDraftViews(mutableListOf<PendingBrokerDraft>(item, item)).size, "views")
    }
}
