package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRestBreakEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRestBreakEstimate("code-702", "title-714")
        val view = toBatchedRestBreakEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRestBreakEstimateViews(mutableListOf<BatchedRestBreakEstimate>(item, item)).size, "views")
    }
}
