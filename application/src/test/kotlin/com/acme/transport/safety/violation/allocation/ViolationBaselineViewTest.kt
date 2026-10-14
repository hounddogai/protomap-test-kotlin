package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationBaseline("code-856", "label-739")
        val view = toViolationBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationBaselineViews(mutableListOf<ViolationBaseline>(item, item)).size, "views")
    }
}
