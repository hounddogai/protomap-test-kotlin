package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDockDoorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        val view = toDefaultDockDoorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDockDoorSegmentViews(mutableListOf<DefaultDockDoorSegment>(item, item)).size, "views")
    }
}
