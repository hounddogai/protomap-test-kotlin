package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyLanePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        val view = toLegacyLanePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyLanePlanTargetViews(mutableListOf<LegacyLanePlanTarget>(item, item)).size, "views")
    }
}
