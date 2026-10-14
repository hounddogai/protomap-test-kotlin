package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDwellPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        val view = toSeasonalDwellPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDwellPolicyViews(mutableListOf<SeasonalDwellPolicy>(item, item)).size, "views")
    }
}
