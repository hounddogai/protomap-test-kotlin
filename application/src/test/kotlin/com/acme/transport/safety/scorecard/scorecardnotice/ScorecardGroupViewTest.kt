package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardGroup("code-601", "name-240")
        val view = toScorecardGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardGroupViews(mutableListOf<ScorecardGroup>(item, item)).size, "views")
    }
}
