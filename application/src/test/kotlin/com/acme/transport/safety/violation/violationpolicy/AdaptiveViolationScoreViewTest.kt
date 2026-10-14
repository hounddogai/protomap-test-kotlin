package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveViolationScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveViolationScore("code-47", "label-863")
        val view = toAdaptiveViolationScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveViolationScoreViews(mutableListOf<AdaptiveViolationScore>(item, item)).size, "views")
    }
}
