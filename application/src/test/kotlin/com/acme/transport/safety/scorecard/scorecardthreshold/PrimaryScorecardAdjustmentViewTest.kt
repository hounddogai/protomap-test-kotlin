package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryScorecardAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        val view = toPrimaryScorecardAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryScorecardAdjustmentViews(mutableListOf<PrimaryScorecardAdjustment>(item, item)).size, "views")
    }
}
