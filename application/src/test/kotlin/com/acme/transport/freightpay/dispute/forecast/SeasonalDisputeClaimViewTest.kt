package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDisputeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDisputeClaim("code-246", "name-309")
        val view = toSeasonalDisputeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDisputeClaimViews(mutableListOf<SeasonalDisputeClaim>(item, item)).size, "views")
    }
}
