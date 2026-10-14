package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardPreference("code-456", "title-363")
        val view = toScorecardPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardPreferenceViews(mutableListOf<ScorecardPreference>(item, item)).size, "views")
    }
}
