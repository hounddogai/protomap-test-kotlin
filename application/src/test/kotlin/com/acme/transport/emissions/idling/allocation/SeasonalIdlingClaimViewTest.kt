package com.acme.transport.emissions.idling.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalIdlingClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalIdlingClaim("code-570", "label-170")
        val view = toSeasonalIdlingClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalIdlingClaimViews(mutableListOf<SeasonalIdlingClaim>(item, item)).size, "views")
    }
}
