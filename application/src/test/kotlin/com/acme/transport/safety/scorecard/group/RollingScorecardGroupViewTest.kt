package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingScorecardGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingScorecardGroup("code-509", "title-479")
        val view = toRollingScorecardGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingScorecardGroupViews(mutableListOf<RollingScorecardGroup>(item, item)).size, "views")
    }
}
