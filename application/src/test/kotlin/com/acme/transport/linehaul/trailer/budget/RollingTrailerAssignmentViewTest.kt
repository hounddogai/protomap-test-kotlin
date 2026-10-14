package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTrailerAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTrailerAssignment("code-714", "name-281")
        val view = toRollingTrailerAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTrailerAssignmentViews(mutableListOf<RollingTrailerAssignment>(item, item)).size, "views")
    }
}
