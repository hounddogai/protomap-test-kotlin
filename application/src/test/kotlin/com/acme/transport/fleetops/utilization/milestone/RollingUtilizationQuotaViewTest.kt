package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingUtilizationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingUtilizationQuota("code-413", "name-979")
        val view = toRollingUtilizationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingUtilizationQuotaViews(mutableListOf<RollingUtilizationQuota>(item, item)).size, "views")
    }
}
