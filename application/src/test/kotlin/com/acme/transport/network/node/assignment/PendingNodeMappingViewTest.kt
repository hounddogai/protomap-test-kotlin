package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingNodeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingNodeMapping("code-872", "title-846")
        val view = toPendingNodeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingNodeMappingViews(mutableListOf<PendingNodeMapping>(item, item)).size, "views")
    }
}
