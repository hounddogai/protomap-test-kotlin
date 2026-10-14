package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRestBreakSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        val view = toBatchedRestBreakSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRestBreakSummaryViews(mutableListOf<BatchedRestBreakSummary>(item, item)).size, "views")
    }
}
