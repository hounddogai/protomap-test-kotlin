package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCutoffClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCutoffClaim("code-917", "name-102")
        val view = toSeasonalCutoffClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCutoffClaimViews(mutableListOf<SeasonalCutoffClaim>(item, item)).size, "views")
    }
}
