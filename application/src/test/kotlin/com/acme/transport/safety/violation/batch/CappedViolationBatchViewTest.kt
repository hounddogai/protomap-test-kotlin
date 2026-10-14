package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedViolationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedViolationBatch("code-565", "label-529")
        val view = toCappedViolationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedViolationBatchViews(mutableListOf<CappedViolationBatch>(item, item)).size, "views")
    }
}
