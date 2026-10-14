package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBackhaulRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBackhaulRule("code-556", "label-208")
        val view = toCappedBackhaulRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBackhaulRuleViews(mutableListOf<CappedBackhaulRule>(item, item)).size, "views")
    }
}
