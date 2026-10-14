package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedUtilizationAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedUtilizationAudit("code-589", "title-36")
        val view = toWeightedUtilizationAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedUtilizationAuditViews(mutableListOf<WeightedUtilizationAudit>(item, item)).size, "views")
    }
}
