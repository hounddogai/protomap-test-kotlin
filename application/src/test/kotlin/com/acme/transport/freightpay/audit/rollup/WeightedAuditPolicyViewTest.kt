package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAuditPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAuditPolicy("code-106", "name-642")
        val view = toWeightedAuditPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAuditPolicyViews(mutableListOf<WeightedAuditPolicy>(item, item)).size, "views")
    }
}
