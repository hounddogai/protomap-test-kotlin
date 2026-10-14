package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationTarget("code-77", "label-181")
        val view = toViolationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationTargetViews(mutableListOf<ViolationTarget>(item, item)).size, "views")
    }
}
