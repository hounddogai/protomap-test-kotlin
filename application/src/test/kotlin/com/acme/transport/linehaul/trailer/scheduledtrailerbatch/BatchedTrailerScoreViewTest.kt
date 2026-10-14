package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerScore("code-957", "label-279")
        val view = toBatchedTrailerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerScoreViews(mutableListOf<BatchedTrailerScore>(item, item)).size, "views")
    }
}
