package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceRule("code-202", "name-424")
        val view = toClearanceRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceRuleViews(mutableListOf<ClearanceRule>(item, item)).size, "views")
    }
}
