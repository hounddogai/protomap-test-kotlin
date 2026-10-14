package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedViolationBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedViolationBaseline("code-659", "name-781")
        val view = toCappedViolationBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedViolationBaselineViews(mutableListOf<CappedViolationBaseline>(item, item)).size, "views")
    }
}
