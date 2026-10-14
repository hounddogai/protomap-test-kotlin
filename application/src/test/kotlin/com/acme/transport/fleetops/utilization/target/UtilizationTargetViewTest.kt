package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationTarget("code-854", "title-250")
        val view = toUtilizationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationTargetViews(mutableListOf<UtilizationTarget>(item, item)).size, "views")
    }
}
