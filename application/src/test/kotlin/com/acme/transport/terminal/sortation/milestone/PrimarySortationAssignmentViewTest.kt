package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimarySortationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimarySortationAssignment("code-367", "label-761")
        val view = toPrimarySortationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimarySortationAssignmentViews(mutableListOf<PrimarySortationAssignment>(item, item)).size, "views")
    }
}
