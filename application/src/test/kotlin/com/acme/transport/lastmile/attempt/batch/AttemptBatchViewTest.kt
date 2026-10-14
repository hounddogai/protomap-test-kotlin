package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptBatch("code-751", "name-215")
        val view = toAttemptBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptBatchViews(mutableListOf<AttemptBatch>(item, item)).size, "views")
    }
}
