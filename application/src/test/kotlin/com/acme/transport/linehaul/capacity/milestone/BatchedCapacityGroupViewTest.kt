package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityGroup("code-554", "label-655")
        val view = toBatchedCapacityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityGroupViews(mutableListOf<BatchedCapacityGroup>(item, item)).size, "views")
    }
}
