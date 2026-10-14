package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTenderAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTenderAllocation("code-729", "label-738")
        val view = toWeightedTenderAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTenderAllocationViews(mutableListOf<WeightedTenderAllocation>(item, item)).size, "views")
    }
}
