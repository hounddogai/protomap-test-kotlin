package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldBatch("code-102", "title-672")
        val view = toHoldBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldBatchViews(mutableListOf<HoldBatch>(item, item)).size, "views")
    }
}
