package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRestBreakRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRestBreakRecord("code-757", "title-371")
        val view = toBatchedRestBreakRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRestBreakRecordViews(mutableListOf<BatchedRestBreakRecord>(item, item)).size, "views")
    }
}
