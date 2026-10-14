package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictHubBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictHubBatch("code-999", "name-864")
        val view = toStrictHubBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictHubBatchViews(mutableListOf<StrictHubBatch>(item, item)).size, "views")
    }
}
