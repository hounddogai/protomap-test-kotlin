package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyNodeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyNodeAssignment("code-13", "name-300")
        val view = toLegacyNodeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyNodeAssignmentViews(mutableListOf<LegacyNodeAssignment>(item, item)).size, "views")
    }
}
