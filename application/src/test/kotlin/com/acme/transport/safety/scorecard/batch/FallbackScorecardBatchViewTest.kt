package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackScorecardBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackScorecardBatch("code-356", "label-53")
        val view = toFallbackScorecardBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackScorecardBatchViews(mutableListOf<FallbackScorecardBatch>(item, item)).size, "views")
    }
}
