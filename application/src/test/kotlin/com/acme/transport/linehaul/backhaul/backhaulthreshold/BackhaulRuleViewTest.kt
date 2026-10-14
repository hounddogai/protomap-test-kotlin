package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulRule("code-313", "name-931")
        val view = toBackhaulRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulRuleViews(mutableListOf<BackhaulRule>(item, item)).size, "views")
    }
}
