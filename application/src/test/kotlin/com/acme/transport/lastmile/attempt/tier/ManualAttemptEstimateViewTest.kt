package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAttemptEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAttemptEstimate("code-438", "label-997")
        val view = toManualAttemptEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAttemptEstimateViews(mutableListOf<ManualAttemptEstimate>(item, item)).size, "views")
    }
}
