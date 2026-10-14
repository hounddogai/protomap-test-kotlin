package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAccessorialAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAccessorialAllocation("code-785", "label-593")
        val view = toWeightedAccessorialAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAccessorialAllocationViews(mutableListOf<WeightedAccessorialAllocation>(item, item)).size, "views")
    }
}
