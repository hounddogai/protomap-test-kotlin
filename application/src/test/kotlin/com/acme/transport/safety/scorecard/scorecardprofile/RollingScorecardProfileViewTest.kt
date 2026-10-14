package com.acme.transport.safety.scorecard.scorecardprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingScorecardProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingScorecardProfile("code-868", "label-665")
        val view = toRollingScorecardProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingScorecardProfileViews(mutableListOf<RollingScorecardProfile>(item, item)).size, "views")
    }
}
