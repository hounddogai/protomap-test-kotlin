package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerAssignment("code-768", "name-740")
        val view = toLockerAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerAssignmentViews(mutableListOf<LockerAssignment>(item, item)).size, "views")
    }
}
