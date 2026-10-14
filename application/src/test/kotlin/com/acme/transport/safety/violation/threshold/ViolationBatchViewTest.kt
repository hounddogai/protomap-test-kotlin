package com.acme.transport.safety.violation.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationBatch("code-136", "label-610")
        val view = toViolationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationBatchViews(mutableListOf<ViolationBatch>(item, item)).size, "views")
    }
}
