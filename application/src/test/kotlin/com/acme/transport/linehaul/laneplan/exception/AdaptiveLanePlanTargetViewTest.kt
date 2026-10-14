package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLanePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLanePlanTarget("code-252", "name-794")
        val view = toAdaptiveLanePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLanePlanTargetViews(mutableListOf<AdaptiveLanePlanTarget>(item, item)).size, "views")
    }
}
