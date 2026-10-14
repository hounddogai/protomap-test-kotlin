package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualViolationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualViolationBatch("code-498", "label-620")
        val view = toManualViolationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualViolationBatchViews(mutableListOf<ManualViolationBatch>(item, item)).size, "views")
    }
}
