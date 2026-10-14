package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerRule("code-827", "label-513")
        val view = toTrailerRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerRuleViews(mutableListOf<TrailerRule>(item, item)).size, "views")
    }
}
