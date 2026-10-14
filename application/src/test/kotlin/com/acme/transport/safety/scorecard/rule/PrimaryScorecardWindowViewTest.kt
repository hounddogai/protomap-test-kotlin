package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryScorecardWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        val view = toPrimaryScorecardWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryScorecardWindowViews(mutableListOf<PrimaryScorecardWindow>(item, item)).size, "views")
    }
}
