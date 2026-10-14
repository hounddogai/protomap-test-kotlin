package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedHoldAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedHoldAllocation("code-182", "title-361")
        val view = toBatchedHoldAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedHoldAllocationViews(mutableListOf<BatchedHoldAllocation>(item, item)).size, "views")
    }
}
