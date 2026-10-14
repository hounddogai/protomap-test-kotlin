package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderRule("code-493", "label-445")
        val view = toTenderRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderRuleViews(mutableListOf<TenderRule>(item, item)).size, "views")
    }
}
