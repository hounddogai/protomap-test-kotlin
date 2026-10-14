package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledNodeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledNodeProfile("code-402", "label-291")
        val view = toScheduledNodeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledNodeProfileViews(mutableListOf<ScheduledNodeProfile>(item, item)).size, "views")
    }
}
