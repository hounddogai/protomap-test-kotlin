package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptAdjustment("code-641", "label-714")
        val view = toAttemptAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptAdjustmentViews(mutableListOf<AttemptAdjustment>(item, item)).size, "views")
    }
}
