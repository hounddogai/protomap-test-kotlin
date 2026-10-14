package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingViolationRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingViolationRollup("code-422", "name-880")
        val view = toPendingViolationRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingViolationRollupViews(mutableListOf<PendingViolationRollup>(item, item)).size, "views")
    }
}
