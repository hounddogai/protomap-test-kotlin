package com.acme.transport.safety.violation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedViolationThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedViolationThreshold("code-495", "title-761")
        val view = toCappedViolationThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedViolationThresholdViews(mutableListOf<CappedViolationThreshold>(item, item)).size, "views")
    }
}
