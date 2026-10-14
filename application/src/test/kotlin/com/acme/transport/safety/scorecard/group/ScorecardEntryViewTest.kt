package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardEntry("code-161", "name-574")
        val view = toScorecardEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardEntryViews(mutableListOf<ScorecardEntry>(item, item)).size, "views")
    }
}
