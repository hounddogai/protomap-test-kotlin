package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNodeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNodeAssignment("code-499", "title-712")
        val view = toDefaultNodeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNodeAssignmentViews(mutableListOf<DefaultNodeAssignment>(item, item)).size, "views")
    }
}
