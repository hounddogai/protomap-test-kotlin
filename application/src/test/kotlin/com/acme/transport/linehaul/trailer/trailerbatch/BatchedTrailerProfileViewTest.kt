package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerProfile("code-516", "label-657")
        val view = toBatchedTrailerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerProfileViews(mutableListOf<BatchedTrailerProfile>(item, item)).size, "views")
    }
}
