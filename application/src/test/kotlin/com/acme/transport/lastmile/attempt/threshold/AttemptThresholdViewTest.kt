package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptThreshold("code-365", "title-255")
        val view = toAttemptThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptThresholdViews(mutableListOf<AttemptThreshold>(item, item)).size, "views")
    }
}
