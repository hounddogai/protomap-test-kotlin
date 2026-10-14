package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryScorecardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        val view = toPrimaryScorecardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryScorecardThresholdViews(mutableListOf<PrimaryScorecardThreshold>(item, item)).size, "views")
    }
}
