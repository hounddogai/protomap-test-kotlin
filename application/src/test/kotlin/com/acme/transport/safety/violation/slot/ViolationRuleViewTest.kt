package com.acme.transport.safety.violation.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationRule("code-432", "label-372")
        val view = toViolationRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationRuleViews(mutableListOf<ViolationRule>(item, item)).size, "views")
    }
}
