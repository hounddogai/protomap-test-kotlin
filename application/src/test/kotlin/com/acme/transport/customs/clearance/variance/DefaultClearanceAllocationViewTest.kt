package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultClearanceAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        val view = toDefaultClearanceAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultClearanceAllocationViews(mutableListOf<DefaultClearanceAllocation>(item, item)).size, "views")
    }
}
