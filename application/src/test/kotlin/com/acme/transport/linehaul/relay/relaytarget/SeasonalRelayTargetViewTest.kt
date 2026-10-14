package com.acme.transport.linehaul.relay.relaytarget

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRelayTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRelayTarget("code-665", "title-842")
        val view = toSeasonalRelayTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRelayTargetViews(mutableListOf<SeasonalRelayTarget>(item, item)).size, "views")
    }
}
