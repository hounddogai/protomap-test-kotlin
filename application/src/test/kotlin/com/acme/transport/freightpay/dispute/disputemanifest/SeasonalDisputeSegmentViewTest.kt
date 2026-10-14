package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDisputeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDisputeSegment("code-934", "title-965")
        val view = toSeasonalDisputeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDisputeSegmentViews(mutableListOf<SeasonalDisputeSegment>(item, item)).size, "views")
    }
}
