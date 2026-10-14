package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBrokerCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBrokerCheckpoint("code-46", "name-327")
        val view = toCappedBrokerCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBrokerCheckpointViews(mutableListOf<CappedBrokerCheckpoint>(item, item)).size, "views")
    }
}
