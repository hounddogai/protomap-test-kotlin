package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryAssignment("code-270", "name-39")
        val view = toSeasonalRedeliveryAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryAssignmentViews(mutableListOf<SeasonalRedeliveryAssignment>(item, item)).size, "views")
    }
}
