package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryUtilizationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryUtilizationTarget("code-280", "label-483")
        val view = toPrimaryUtilizationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryUtilizationTargetViews(mutableListOf<PrimaryUtilizationTarget>(item, item)).size, "views")
    }
}
