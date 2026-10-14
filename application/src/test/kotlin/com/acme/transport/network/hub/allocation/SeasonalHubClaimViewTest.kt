package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalHubClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalHubClaim("code-507", "title-385")
        val view = toSeasonalHubClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalHubClaimViews(mutableListOf<SeasonalHubClaim>(item, item)).size, "views")
    }
}
