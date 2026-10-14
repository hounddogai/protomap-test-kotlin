package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRestBreakAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRestBreakAssignment("code-504", "name-163")
        val view = toFallbackRestBreakAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRestBreakAssignmentViews(mutableListOf<FallbackRestBreakAssignment>(item, item)).size, "views")
    }
}
