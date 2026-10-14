package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTrailerBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTrailerBatch("code-404", "title-121")
        val view = toWeightedTrailerBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTrailerBatchViews(mutableListOf<WeightedTrailerBatch>(item, item)).size, "views")
    }
}
