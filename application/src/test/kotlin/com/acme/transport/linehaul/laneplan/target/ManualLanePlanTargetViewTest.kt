package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualLanePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualLanePlanTarget("code-743", "label-565")
        val view = toManualLanePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualLanePlanTargetViews(mutableListOf<ManualLanePlanTarget>(item, item)).size, "views")
    }
}
