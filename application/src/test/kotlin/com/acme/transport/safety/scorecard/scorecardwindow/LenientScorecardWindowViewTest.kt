package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientScorecardWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientScorecardWindow("code-81", "title-319")
        val view = toLenientScorecardWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientScorecardWindowViews(mutableListOf<LenientScorecardWindow>(item, item)).size, "views")
    }
}
