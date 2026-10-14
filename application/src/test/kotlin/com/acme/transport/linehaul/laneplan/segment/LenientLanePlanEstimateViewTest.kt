package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientLanePlanEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        val view = toLenientLanePlanEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientLanePlanEstimateViews(mutableListOf<LenientLanePlanEstimate>(item, item)).size, "views")
    }
}
