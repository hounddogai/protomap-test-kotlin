package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class DropAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropAssignment("code-255", "label-951")
        val view = toDropAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropAssignmentViews(mutableListOf<DropAssignment>(item, item)).size, "views")
    }
}
