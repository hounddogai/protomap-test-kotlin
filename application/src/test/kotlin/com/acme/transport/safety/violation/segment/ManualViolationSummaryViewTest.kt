package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualViolationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualViolationSummary("code-76", "name-117")
        val view = toManualViolationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualViolationSummaryViews(mutableListOf<ManualViolationSummary>(item, item)).size, "views")
    }
}
