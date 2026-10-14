package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalServicePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        val view = toGlobalServicePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalServicePlanTargetViews(mutableListOf<GlobalServicePlanTarget>(item, item)).size, "views")
    }
}
