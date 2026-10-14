package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalBrokerCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalBrokerCheckpoint("code-884", "label-124")
        val view = toRegionalBrokerCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalBrokerCheckpointViews(mutableListOf<RegionalBrokerCheckpoint>(item, item)).size, "views")
    }
}
