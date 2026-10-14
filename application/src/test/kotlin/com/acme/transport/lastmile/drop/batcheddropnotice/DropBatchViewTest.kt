package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class DropBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropBatch("code-143", "title-771")
        val view = toDropBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropBatchViews(mutableListOf<DropBatch>(item, item)).size, "views")
    }
}
