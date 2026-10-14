package com.acme.transport.crewing.availability.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityRule("code-703", "title-857")
        val view = toAvailabilityRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityRuleViews(mutableListOf<AvailabilityRule>(item, item)).size, "views")
    }
}
