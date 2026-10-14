package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBrokerCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBrokerCheckpoint("code-961", "name-940")
        val view = toPendingBrokerCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBrokerCheckpointViews(mutableListOf<PendingBrokerCheckpoint>(item, item)).size, "views")
    }
}
