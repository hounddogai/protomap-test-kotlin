package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualIdlingRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualIdlingRule("code-100", "title-89")
        val view = toManualIdlingRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualIdlingRuleViews(mutableListOf<ManualIdlingRule>(item, item)).size, "views")
    }
}
