package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictServicePlanClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictServicePlanClaim("code-443", "label-584")
        val view = toStrictServicePlanClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictServicePlanClaimViews(mutableListOf<StrictServicePlanClaim>(item, item)).size, "views")
    }
}
