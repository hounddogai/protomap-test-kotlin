package com.acme.transport.freightpay.audit.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAuditMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAuditMapping("code-240", "title-970")
        val view = toWeightedAuditMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAuditMappingViews(mutableListOf<WeightedAuditMapping>(item, item)).size, "views")
    }
}
