package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDropRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDropRule("code-477", "label-518")
        val view = toPrimaryDropRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDropRuleViews(mutableListOf<PrimaryDropRule>(item, item)).size, "views")
    }
}
