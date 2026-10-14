package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingViolationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingViolationBatch("code-767", "label-260")
        val view = toRollingViolationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingViolationBatchViews(mutableListOf<RollingViolationBatch>(item, item)).size, "views")
    }
}
