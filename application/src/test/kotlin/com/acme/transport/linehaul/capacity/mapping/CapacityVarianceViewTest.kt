package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityVariance("code-863", "label-375")
        val view = toCapacityVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityVarianceViews(mutableListOf<CapacityVariance>(item, item)).size, "views")
    }
}
