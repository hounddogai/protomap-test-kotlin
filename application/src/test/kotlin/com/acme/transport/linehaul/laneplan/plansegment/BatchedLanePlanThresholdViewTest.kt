package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLanePlanThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        val view = toBatchedLanePlanThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLanePlanThresholdViews(mutableListOf<BatchedLanePlanThreshold>(item, item)).size, "views")
    }
}
