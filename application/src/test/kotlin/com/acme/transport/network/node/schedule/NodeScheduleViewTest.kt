package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeSchedule("code-489", "label-945")
        val view = toNodeScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeScheduleViews(mutableListOf<NodeSchedule>(item, item)).size, "views")
    }
}
