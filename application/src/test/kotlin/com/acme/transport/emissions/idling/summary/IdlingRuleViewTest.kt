package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingRule("code-538", "title-66")
        val view = toIdlingRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingRuleViews(mutableListOf<IdlingRule>(item, item)).size, "views")
    }
}
