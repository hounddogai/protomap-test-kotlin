package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedIdlingThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        val view = toBatchedIdlingThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedIdlingThresholdViews(mutableListOf<BatchedIdlingThreshold>(item, item)).size, "views")
    }
}
