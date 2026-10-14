package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationRollup("code-877", "label-40")
        val view = toViolationRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationRollupViews(mutableListOf<ViolationRollup>(item, item)).size, "views")
    }
}
