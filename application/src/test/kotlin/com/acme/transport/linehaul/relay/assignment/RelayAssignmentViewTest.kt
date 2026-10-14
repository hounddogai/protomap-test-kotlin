package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayAssignment("code-8", "title-657")
        val view = toRelayAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayAssignmentViews(mutableListOf<RelayAssignment>(item, item)).size, "views")
    }
}
