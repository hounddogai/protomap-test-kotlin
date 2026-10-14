package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCapacityPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        val view = toAdaptiveCapacityPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCapacityPolicyViews(mutableListOf<AdaptiveCapacityPolicy>(item, item)).size, "views")
    }
}
