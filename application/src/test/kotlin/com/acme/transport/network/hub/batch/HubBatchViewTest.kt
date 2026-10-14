package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class HubBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubBatch("code-160", "name-559")
        val view = toHubBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubBatchViews(mutableListOf<HubBatch>(item, item)).size, "views")
    }
}
