package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulAllocation("code-624", "label-124")
        val view = toBackhaulAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulAllocationViews(mutableListOf<BackhaulAllocation>(item, item)).size, "views")
    }
}
