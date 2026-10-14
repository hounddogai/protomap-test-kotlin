package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingSwapRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingSwapRecord("code-762", "label-436")
        val view = toPendingSwapRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingSwapRecordViews(mutableListOf<PendingSwapRecord>(item, item)).size, "views")
    }
}
