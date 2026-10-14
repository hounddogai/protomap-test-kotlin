package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultViolationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultViolationSegment("code-498", "name-775")
        val view = toDefaultViolationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultViolationSegmentViews(mutableListOf<DefaultViolationSegment>(item, item)).size, "views")
    }
}
