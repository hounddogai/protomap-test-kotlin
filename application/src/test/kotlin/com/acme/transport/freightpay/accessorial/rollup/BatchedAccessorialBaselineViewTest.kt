package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAccessorialBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAccessorialBaseline("code-345", "label-683")
        val view = toBatchedAccessorialBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAccessorialBaselineViews(mutableListOf<BatchedAccessorialBaseline>(item, item)).size, "views")
    }
}
