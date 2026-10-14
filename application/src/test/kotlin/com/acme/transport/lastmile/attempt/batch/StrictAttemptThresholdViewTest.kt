package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAttemptThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAttemptThreshold("code-848", "label-737")
        val view = toStrictAttemptThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAttemptThresholdViews(mutableListOf<StrictAttemptThreshold>(item, item)).size, "views")
    }
}
