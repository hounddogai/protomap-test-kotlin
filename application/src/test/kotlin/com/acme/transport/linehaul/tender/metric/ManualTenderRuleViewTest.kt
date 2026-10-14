package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTenderRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTenderRule("code-33", "label-760")
        val view = toManualTenderRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTenderRuleViews(mutableListOf<ManualTenderRule>(item, item)).size, "views")
    }
}
